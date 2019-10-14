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

import discord4j.common.close.CloseException;
import discord4j.common.close.CloseStatus;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.context.Context;

import static discord4j.common.LogUtil.format;

/**
 * Represents a WebSocket handler specialized for Discord gateway operations.
 * <p>
 * Includes a zlib-based decompressor and dedicated handling of closing events that normally occur during Discord
 * gateway lifecycle.
 * <p>
 * This handler uses a {@link FluxSink} of {@link ByteBuf} to push inbound payloads and a {@link Flux} of
 * {@link ByteBuf} to pull outbound payloads.
 * <p>
 * The handler also provides two methods to control the lifecycle and proper cleanup, like {@link #close()} and
 * {@link #error(Throwable)} which perform operations on the current session. It is necessary to use these methods in
 * order to signal closure or errors and cleanly complete the session.
 */
public class DiscordWebSocketHandler {

    private final FluxSink<ByteBuf> inbound;
    private final Flux<ByteBuf> outbound;
    private final MonoProcessor<CloseStatus> closeTrigger;
    private final MonoProcessor<Void> completionNotifier = MonoProcessor.create();
    private final ZlibDecompressor decompressor = new ZlibDecompressor();
    private final Context context;

    /**
     * Create a new handler with the given data pipelines.
     *
     * @param inbound the {@link FluxSink} of {@link ByteBuf} to process inbound payloads
     * @param outbound the {@link Flux} of {@link ByteBuf} to process outbound payloads
     * @param closeTrigger a {@link MonoProcessor} that triggers the closing of this session
     * @param context the Reactor {@link Context} that owns this handler, to enrich logging
     */
    public DiscordWebSocketHandler(FluxSink<ByteBuf> inbound, Flux<ByteBuf> outbound,
                                   MonoProcessor<CloseStatus> closeTrigger, Context context) {
        this.inbound = inbound;
        this.outbound = outbound;
        this.closeTrigger = closeTrigger;
        this.context = context;
    }

    public Mono<Void> handle(WebsocketInbound in, WebsocketOutbound out) {
        Mono<CloseWebSocketFrame> outboundClose = closeTrigger
                .map(status -> new CloseWebSocketFrame(status.getCode(), status.getReason()));

        Mono<CloseStatus> inboundClose = in.receiveCloseStatus()
                .map(status -> new CloseStatus(status.code(), status.reasonText()));

        Mono<Void> outboundEvents = out.sendObject(Flux.merge(outboundClose, outbound.map(TextWebSocketFrame::new)))
                .then()
                .then(Mono.defer(() -> {
                    log.info(format(context, "Sender completed"));
                    return inboundClose.filter(__ -> !closeTrigger.isTerminated())
                            .doOnNext(closeStatus -> {
                                log.info(format(context, "Outbound close reason: {}"), closeStatus);
                                error(new CloseException(closeStatus));
                            })
                            .switchIfEmpty(Mono.fromRunnable(() -> error(new RuntimeException("Sender completed"))))
                            .then();
                }));

        Mono<Void> inboundEvents = in.aggregateFrames()
                .receiveFrames()
                .map(WebSocketFrame::content)
                .transformDeferred(decompressor::completeMessages)
                .doOnNext(inbound::next)
                .doOnError(this::error)
                .then(Mono.<Void>defer(() -> {
                    log.info(format(context, "Receiver completed"));
                    return inboundClose.filter(__ -> !closeTrigger.isTerminated())
                            .flatMap(closeStatus -> {
                                log.info(format(context, "Inbound close reason: {}"), closeStatus);
                                return Mono.error(new CloseException(closeStatus));
                            });
                }));

        return Mono.zip(completionNotifier, outboundEvents, inboundEvents)
                .doOnError(t -> log.debug(format(context, "WebSocket session threw an error: {}"), t.toString()))
                .then();
    }

    /**
     * Initiates a close sequence that will terminate this session. It will notify all exchanges and the session
     * completion {@link reactor.core.publisher.Mono} in
     * {@link #handle(reactor.netty.http.websocket.WebsocketInbound, reactor.netty.http.websocket.WebsocketOutbound)}
     * through a complete signal, dropping all future signals.
     */
    public void close() {
        log.info(format(context, "Triggering close sequence"));
        closeTrigger.onNext(CloseStatus.NORMAL_CLOSE);
        completionNotifier.onComplete();
    }

    /**
     * Initiates a close sequence with the given error. It will terminate this session with an error signal on the
     * {@link #handle(reactor.netty.http.websocket.WebsocketInbound, reactor.netty.http.websocket.WebsocketOutbound)}
     * method, while completing both exchanges through normal complete signals.
     * <p>
     * The error can then be channeled downstream and acted upon accordingly.
     *
     * @param error the cause for this session termination
     */
    public void error(Throwable error) {
        log.warn(format(context, "Triggering error sequence ({})"), error.toString());
        if (!completionNotifier.isTerminated()) {
            if (error instanceof CloseException) {
                completionNotifier.onError(error);
            } else {
                completionNotifier.onError(new CloseException(new CloseStatus(1006, error.toString()), error));
            }
        }
    }

    private static final Logger log = Loggers.getLogger("discord4j.gateway.session");
}
