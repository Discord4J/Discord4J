/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.gateway;

import discord4j.common.close.CloseStatus;
import discord4j.common.close.DisconnectBehavior;
import discord4j.common.sinks.EmissionStrategy;
import discord4j.gateway.retry.GatewayException;
import discord4j.gateway.retry.PartialDisconnectException;
import discord4j.gateway.retry.ReconnectException;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.IllegalReferenceCountException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.context.ContextView;
import reactor.util.function.Tuple2;

import java.time.Duration;

import static discord4j.common.LogUtil.format;
import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

/**
 * Represents a WebSocket handler specialized for Discord gateway operations.
 * <p>
 * Includes a zlib-based decompressor and dedicated handling of closing events that normally occur during Discord
 * gateway lifecycle.
 * <p>
 * This handler uses a {@link FluxSink} of {@link ByteBuf} to push inbound payloads and a {@link Flux} of
 * {@link ByteBuf} to pull outbound payloads.
 * <p>
 * The handler also provides methods to control the lifecycle, which perform operations on the current session. It is
 * required to use them to properly release important resources and complete the session.
 */
public class GatewayWebsocketHandler {

    private static final Logger log = Loggers.getLogger(GatewayWebsocketHandler.class);

    private final Sinks.Many<ByteBuf> inbound;
    private final Flux<ByteBuf> outbound;
    private final Sinks.One<DisconnectBehavior> sessionClose;
    private final ContextView context;
    private final boolean unpooled;
    private final EmissionStrategy emissionStrategy;

    /**
     * Create a new handler with the given data pipelines.
     *
     * @param inbound the {@link reactor.core.publisher.Sinks.Many} of {@link ByteBuf} to process inbound payloads
     * @param outbound the {@link Flux} of {@link ByteBuf} to process outbound payloads
     * @param context the Reactor {@link ContextView} that owns this handler, to enrich logging
     */
    public GatewayWebsocketHandler(Sinks.Many<ByteBuf> inbound, Flux<ByteBuf> outbound, ContextView context) {
        this(inbound, outbound, context, false);
    }

    public GatewayWebsocketHandler(Sinks.Many<ByteBuf> inbound, Flux<ByteBuf> outbound, ContextView context,
                                   boolean unpooled) {
        this.inbound = inbound;
        this.outbound = outbound;
        this.sessionClose = Sinks.one();
        this.context = context;
        this.unpooled = unpooled;
        this.emissionStrategy = EmissionStrategy.park(Duration.ofNanos(10));
    }

    /**
     * Handle an upgraded websocket connection, given by both {@link WebsocketInbound} and {@link WebsocketOutbound} to
     * manage a session until the remote closes or one of the local methods {@link #close()} or
     * {@link #error(Throwable)} methods are called. When that happens, a close procedure will take place and ultimately
     * emit a pair of {@link DisconnectBehavior} and remote {@link CloseStatus}, if present or "-1" if none is present.
     *
     * @param in the websocket inbound
     * @param out the websocket outbound
     * @return a {@link Mono} that upon subscription, manages a websocket session until it closes where a {@link Tuple2}
     * is emitted representing both the {@link DisconnectBehavior} that initiated the close procedure, and the inbound
     * {@link CloseStatus}.
     */
    public Mono<Tuple2<DisconnectBehavior, CloseStatus>> handle(WebsocketInbound in, WebsocketOutbound out) {
        ZlibDecompressor decompressor = new ZlibDecompressor(out.alloc());

        Mono<CloseWebSocketFrame> outboundClose = sessionClose.asMono()
                .doOnNext(behavior -> log.debug(format(context, "Closing session with behavior: {}"), behavior))
                .flatMap(behavior -> {
                    switch (behavior.getAction()) {
                        case RETRY_ABRUPTLY:
                        case STOP_ABRUPTLY:
                            return Mono.error(behavior.getCause() != null ? behavior.getCause() :
                                    new PartialDisconnectException(context));
                        case RETRY:
                        case STOP:
                        default:
                            return Mono.just(CloseStatus.NORMAL_CLOSE);
                    }
                })
                .map(status -> new CloseWebSocketFrame(status.getCode(), status.getReason().orElse(null)));

        Mono<CloseStatus> inboundClose = in.receiveCloseStatus()
                .map(status -> new CloseStatus(status.code(), status.reasonText()))
                .doOnNext(status -> log.debug(format(context, "Received close status: {}"), status))
                .doOnNext(status -> close(DisconnectBehavior.retryAbruptly(
                        new GatewayException(context, "Inbound close status"))));

        Mono<Void> outboundEvents = out.sendObject(Flux.merge(outboundClose, outbound.map(TextWebSocketFrame::new)))
                .then();

        in.withConnection(c -> c.onDispose(() -> log.debug(format(context, "Connection disposed"))));

        Mono<Void> inboundEvents = in.aggregateFrames()
                .receiveFrames()
                .map(WebSocketFrame::content)
                .transformDeferred(decompressor::completeMessages)
                .doOnNext(this::emitInbound)
                .doOnNext(this::safeRelease)
                .then();

        return Mono.zip(outboundEvents, inboundEvents)
                .doOnError(this::error)
                .onErrorResume(t -> t.getCause() instanceof GatewayException, t -> Mono.empty())
                .then(Mono.zip(sessionClose.asMono(), inboundClose.defaultIfEmpty(CloseStatus.ABNORMAL_CLOSE)));
    }

    private void emitInbound(ByteBuf value) {
        if (!emissionStrategy.emitNext(inbound, value)) {
            safeRelease(value);
        }
    }

    /**
     * Initiates a close sequence that will terminate this session and instruct consumers downstream that a reconnect
     * should take place afterwards.
     */
    public void close() {
        close(DisconnectBehavior.retry(null));
    }

    /**
     * Initiates a close sequence that will terminate this session and then execute a given {@link DisconnectBehavior}.
     *
     * @param behavior the {@link DisconnectBehavior} to follow after the close sequence starts
     */
    public void close(DisconnectBehavior behavior) {
        sessionClose.emitValue(behavior, FAIL_FAST);
    }

    /**
     * Initiates a close sequence with the given error. The session will be terminated abruptly and then instruct
     * consumers downstream that a reconnect should take place afterwards.
     *
     * @param error the cause for this session termination
     */
    public void error(Throwable error) {
        if (!(error instanceof ReconnectException)) {
            log.info(format(context, "Triggering error sequence: {}"), error.toString());
        }
        close(DisconnectBehavior.retryAbruptly(error));
    }

    private void safeRelease(ByteBuf buf) {
        if (!unpooled && buf.refCnt() > 0) {
            try {
                buf.release();
            } catch (IllegalReferenceCountException e) {
                if (log.isDebugEnabled()) {
                    log.debug("", e);
                }
            }
        }
    }
}
