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

import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.Opcode;
import discord4j.gateway.json.PayloadData;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.ssl.SslCloseCompletionEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.publisher.UnicastProcessor;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.NettyPipeline;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * Represents a websocket handler specialized for Discord gateway operations.
 * <p>
 * It includes a zlib-based decompressor and dedicated handling of closing events that normally occur during Discord
 * gateway lifecycle.
 * <p>
 * This handler provides two {@link reactor.core.publisher.UnicastProcessor} instances for inbound and outbound
 * payload operations. Clients are expected to make proper use of both exchanges, therefore "pull" operations only on
 * the inbound exchange (subscribe), and "push" operations only on the outbound exchange (onNext, onError, onComplete).
 * <p>
 * The handler also provides two methods to control the lifecycle and proper cleanup, like {@link #close()} and
 * {@link #error(Throwable)} which perform operations over both exchanges and the current session. It is required to
 * use these methods to signal closure and errors in order to cleanly complete the session.
 * <p>
 * All payloads going through this handler are passed to the given {@link discord4j.gateway.payload.PayloadReader}
 * and {@link discord4j.gateway.payload.PayloadWriter}.
 * <h2>Example usage</h2>
 * <pre>
 * // pull operation coming inbound
 * handler.inbound().subscribe(payload -&gt; {
 *     if (payload.getData() instanceof Hello) {
 *         IdentifyProperties properties = new IdentifyProperties(...);
 *         GatewayPayload&lt;Identify&gt; identify = GatewayPayload.identify(...);
 *
 *         handler.outbound().onNext(identify); // push operation going outbound
 *     }
 * }, error -&gt; {
 *     log.warn("Gateway connection terminated: {}", error.toString());
 * });
 * </pre>
 */
public class DiscordWebSocketHandler implements ConnectionObserver {

    private static final Logger log = Loggers.getLogger(DiscordWebSocketHandler.class);
    private static final Logger inboundLogger = Loggers.getLogger("discord4j.gateway.session.inbound");
    private static final Logger outboundLogger = Loggers.getLogger("discord4j.gateway.session.outbound");

    private static final String CLOSE_HANDLER = "client.last.closeHandler";

    private final ZlibDecompressor decompressor = new ZlibDecompressor();
    private final UnicastProcessor<GatewayPayload<?>> inboundExchange = UnicastProcessor.create();
    private final UnicastProcessor<GatewayPayload<?>> outboundExchange = UnicastProcessor.create();
    private final MonoProcessor<Void> completionNotifier = MonoProcessor.create();
    private final TokenBucket limiter = new TokenBucket(120, Duration.ofSeconds(60));

    private final PayloadReader reader;
    private final PayloadWriter writer;

    /**
     * Create a new handler with the given payload reader and writer.
     *
     * @param reader the PayloadReader to process each inbound payload
     * @param writer the PayloadWriter to process each outbound payload
     */
    public DiscordWebSocketHandler(PayloadReader reader, PayloadWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public Mono<Void> handle(WebsocketInbound in, WebsocketOutbound out) {
        AtomicReference<CloseStatus> reason = new AtomicReference<>();
        in.withConnection(connection -> connection.addHandlerLast(CLOSE_HANDLER, new CloseHandlerAdapter(reason)));

        out.options(NettyPipeline.SendOptions::flushOnEach)
                .sendObject(outboundExchange.concatMap(this::limitRate)
                        .log(outboundLogger, Level.FINE, false)
                        .flatMap(this::toOutboundFrame))
                .then()
                .doOnError(t -> outboundLogger.debug("Sender encountered an error"))
                .doOnSuccess(v -> outboundLogger.debug("Sender succeeded"))
                .doOnCancel(() -> outboundLogger.debug("Sender cancelled"))
                .doOnTerminate(() -> outboundLogger.debug("Sender terminated"))
                .subscribe();

        return in.aggregateFrames()
                .receiveFrames()
                .map(WebSocketFrame::content)
                .compose(decompressor::completeMessages)
                .map(reader::read)
                .log(inboundLogger, Level.FINE, false)
                .doOnNext(inboundExchange::onNext)
                .doOnError(t -> inboundLogger.debug("Receiver encountered an error: {}", t.toString()))
                .doOnError(this::error)
                .doOnComplete(() -> {
                    inboundLogger.debug("Receiver completed");
                    CloseStatus closeStatus = reason.get();
                    if (closeStatus != null) {
                        inboundLogger.debug("Forwarding close reason: {}", closeStatus);
                        error(new CloseException(closeStatus));
                    }
                })
                .doOnCancel(() -> inboundLogger.debug("Receiver cancelled"))
                .doOnCancel(inboundExchange::cancel)
                .doOnTerminate(() -> inboundLogger.debug("Receiver terminated"))
                .then(completionNotifier);
    }

    private Publisher<? extends GatewayPayload<? extends PayloadData>> limitRate(GatewayPayload<?> payload) {
        boolean success = limiter.tryConsume(1);
        if (success) {
            return Mono.just(payload);
        } else {
            return Mono.delay(Duration.ofMillis(limiter.delayMillisToConsume(1)))
                    .map(x -> limiter.tryConsume(1))
                    .map(consumed -> payload);
        }
    }

    private Publisher<?> toOutboundFrame(GatewayPayload<? extends PayloadData> payload) {
        // TODO: polish as reactor-netty outbound.sendClose(...) becomes stable
        if (payload.getOp() == null) {
            return Flux.just(new CloseWebSocketFrame(1000, "Logging off"));
        } else if (Opcode.RECONNECT.equals(payload.getOp())) {
            error(new RuntimeException("Reconnecting due to user action"));
            return Flux.empty();
        } else {
            return Flux.just(writer.write(payload)).map(TextWebSocketFrame::new);
        }
    }

    /**
     * Initiates a close sequence that will terminate this session. It will notify all exchanges and the session
     * completion {@link reactor.core.publisher.Mono} in
     * {@link #handle(reactor.netty.http.websocket.WebsocketInbound, reactor.netty.http.websocket.WebsocketOutbound)}
     * through a complete signal, dropping all future signals.
     */
    public void close() {
        log.debug("Triggering close sequence - signaling completion notifier");
        completionNotifier.onComplete();
        log.debug("Preparing to complete outbound exchange after close");
        outboundExchange.onComplete();
        log.debug("Preparing to complete inbound exchange after close");
        inboundExchange.onComplete();
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
        log.debug("Triggering error sequence ({})", error.toString());
        if (!completionNotifier.isTerminated()) {
            if (error instanceof CloseException) {
                log.debug("Signaling completion notifier as error with same CloseException");
                completionNotifier.onError(error);
            } else {
                log.debug("Signaling completion notifier as error with wrapping CloseException");
                completionNotifier.onError(new CloseException(new CloseStatus(1006, error.toString()), error));
            }
        }
        outboundExchange.onNext(new GatewayPayload<>());
        log.debug("Preparing to complete outbound exchange after error");
        outboundExchange.onComplete();
        log.debug("Preparing to complete inbound exchange after error");
        inboundExchange.onComplete();
    }

    @Override
    public void onStateChange(Connection connection, State newState) {
        log.debug("{} {}", newState, connection);
    }

    /**
     * Obtains the processor dedicated to all inbound (coming from the wire) payloads, which is meant to be operated
     * downstream through pull operators only, i.e. a {@link reactor.core.publisher.UnicastProcessor#subscribe()} call.
     *
     * @return the unicast processor with a stream of inbound payloads
     */
    public UnicastProcessor<GatewayPayload<?>> inbound() {
        return inboundExchange;
    }

    /**
     * Obtains the processor dedicated to all outbound (going to the wire) payloads, which is meant to be operated
     * downstream through push operations only, i.e. {@link reactor.core.publisher.UnicastProcessor#onNext(Object)}
     * calls to supply a new payload.
     *
     * @return the unicast processor with a stream of outbound payloads
     */
    public UnicastProcessor<GatewayPayload<?>> outbound() {
        return outboundExchange;
    }

    private static class CloseHandlerAdapter extends ChannelInboundHandlerAdapter {

        private final AtomicReference<CloseStatus> closeStatus;

        private CloseHandlerAdapter(AtomicReference<CloseStatus> closeStatus) {
            this.closeStatus = closeStatus;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof CloseWebSocketFrame && ((CloseWebSocketFrame) msg).isFinalFragment()) {
                CloseWebSocketFrame close = (CloseWebSocketFrame) msg;
                log.debug("Close status: {} {}", close.statusCode(), close.reasonText());
                closeStatus.set(new CloseStatus(close.statusCode(), close.reasonText()));
            }
            ctx.fireChannelRead(msg);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt instanceof SslCloseCompletionEvent) {
                SslCloseCompletionEvent closeEvent = (SslCloseCompletionEvent) evt;
                if (!closeEvent.isSuccess()) {
                    log.debug("Abnormal close status: {}", closeEvent.cause().toString());
                    closeStatus.set(new CloseStatus(1006, closeEvent.cause().toString()));
                }
            }
            ctx.fireUserEventTriggered(evt);
        }
    }
}
