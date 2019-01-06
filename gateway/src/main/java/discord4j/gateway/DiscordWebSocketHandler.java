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
import discord4j.common.close.CloseHandlerAdapter;
import discord4j.common.close.CloseStatus;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.Heartbeat;
import discord4j.gateway.json.Opcode;
import discord4j.gateway.json.PayloadData;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.scheduler.Schedulers;
import reactor.netty.NettyPipeline;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * Represents a WebSocket handler specialized for Discord gateway operations.
 * <p>
 * Includes a zlib-based decompressor and dedicated handling of closing events that normally occur during Discord
 * gateway lifecycle.
 * <p>
 * This handler uses a {@link reactor.core.publisher.FluxSink} of {@link discord4j.gateway.json.GatewayPayload} to
 * push inbound payloads and a {@link reactor.core.publisher.Flux} of {@link discord4j.gateway.json.GatewayPayload} to
 * pull outbound payloads.
 * <p>
 * The handler also provides two methods to control the lifecycle and proper cleanup, like {@link #close()} and
 * {@link #error(Throwable)} which perform operations on the current session. It is necessary to use these methods in
 * order to signal closure or errors and cleanly complete the session.
 * <p>
 * All payloads going through this handler are passed to the given {@link discord4j.gateway.payload.PayloadReader}
 * and {@link discord4j.gateway.payload.PayloadWriter}.
 */
public class DiscordWebSocketHandler {

    private static final String HANDLER = "client.last.closeHandler";
    private static final String OUTBOUND_CAPACITY_PROPERTY = "discord4j.gateway.outbound.capacity";
    private static final String OUTBOUND_DELAY_PROPERTY = "discord4j.gateway.outbound.delay.ms";

    private final ZlibDecompressor decompressor = new ZlibDecompressor();
    private final MonoProcessor<Void> completionNotifier = MonoProcessor.create();

    private final PayloadReader reader;
    private final PayloadWriter writer;
    private final FluxSink<GatewayPayload<?>> inbound;
    private final Flux<GatewayPayload<?>> outbound;
    private final Flux<GatewayPayload<Heartbeat>> heartbeat;
    private final GatewayLimiter identifyLimiter;
    private final GatewayLimiter outboundLimiter;
    private final long outboundDelayMillis;

    private final Logger mainLog;
    private final Logger inLog;
    private final Logger outLog;

    /**
     * Create a new handler with the given payload reader, payload writer and payload exchanges.
     *
     * @param reader the PayloadReader to process each inbound payload
     * @param writer the PayloadWriter to process each outbound payload
     * @param inbound the FluxSink of GatewayPayloads to process inbound payloads
     * @param outbound the Flux of GatewayPayloads to process outbound payloads
     * @param heartbeat the Flux of heartbeat payloads
     * @param shardIndex the shard index of this connection, for tracing
     * @param limiter a GatewayLimiter to throttle IDENTIFY requests
     */
    public DiscordWebSocketHandler(PayloadReader reader, PayloadWriter writer,
            FluxSink<GatewayPayload<?>> inbound, Flux<GatewayPayload<?>> outbound,
            Flux<GatewayPayload<Heartbeat>> heartbeat, int shardIndex, GatewayLimiter limiter) {
        this.mainLog = Loggers.getLogger("discord4j.gateway." + shardIndex);
        this.inLog = Loggers.getLogger("discord4j.gateway.inbound." + shardIndex);
        this.outLog = Loggers.getLogger("discord4j.gateway.outbound." + shardIndex);
        this.reader = reader;
        this.writer = writer;
        this.inbound = inbound;
        this.outbound = outbound;
        this.heartbeat = heartbeat;
        this.identifyLimiter = limiter;
        this.outboundLimiter = new SimpleBucket(outboundLimiterCapacity(), Duration.ofSeconds(60));
        this.outboundDelayMillis = outboundDelayMillis();
    }

    private long outboundLimiterCapacity() {
        String capacityValue = System.getProperty(OUTBOUND_CAPACITY_PROPERTY);
        if (capacityValue != null) {
            try {
                long capacity = Long.valueOf(capacityValue);
                mainLog.info("Overriding default outbound limiter capacity: {}", capacity);
            } catch (NumberFormatException e) {
                mainLog.warn("Invalid custom outbound limiter capacity: {}", capacityValue);
            }
        }
        return 115;
    }

    private long outboundDelayMillis() {
        String delayValue = System.getProperty(OUTBOUND_DELAY_PROPERTY);
        if (delayValue != null) {
            try {
                long value = Long.valueOf(delayValue);
                mainLog.info("Overriding default outbound delay: {}", value);
            } catch (NumberFormatException e) {
                mainLog.warn("Invalid custom outbound delay: {}", delayValue);
            }
        }
        return 10;
    }

    public Mono<Void> handle(WebsocketInbound in, WebsocketOutbound out) {
        AtomicReference<CloseStatus> reason = new AtomicReference<>();
        in.withConnection(connection -> connection.addHandlerLast(HANDLER, new CloseHandlerAdapter(reason, mainLog)));

        Mono<Void> outboundEvents = out.options(NettyPipeline.SendOptions::flushOnEach)
                .sendObject(Flux.merge(heartbeat, outbound.concatMap(this::limitRate, 1))
                        .log(outLog, Level.FINE, false)
                        .flatMap(this::toOutboundFrame))
                .then()
                .doOnError(t -> outLog.debug("Sender threw an error: {}", t.toString()))
                .doOnSuccess(v -> outLog.debug("Sender succeeded"))
                .doOnTerminate(() -> outLog.debug("Sender terminated"));

        Mono<Void> inboundEvents = in.aggregateFrames()
                .receiveFrames()
                .map(WebSocketFrame::content)
                .compose(decompressor::completeMessages)
                .map(reader::read)
                .log(inLog, Level.FINE, false)
                .doOnNext(inbound::next)
                .doOnError(t -> inLog.debug("Receiver threw an error: {}", t.toString()))
                .doOnError(this::error)
                .doOnComplete(() -> {
                    inLog.debug("Receiver completed");
                    CloseStatus closeStatus = reason.get();
                    if (closeStatus != null) {
                        inLog.debug("Forwarding close reason: {}", closeStatus);
                        error(new CloseException(closeStatus));
                    }
                })
                .doOnTerminate(() -> inLog.debug("Receiver terminated"))
                .then();

        return Mono.zip(completionNotifier, outboundEvents, inboundEvents)
                .doOnError(t -> mainLog.debug("WebSocket session threw an error: {}", t.toString()))
                .then();
    }

    private Publisher<? extends GatewayPayload<? extends PayloadData>> limitRate(GatewayPayload<?> payload) {
        GatewayLimiter limiter = Opcode.IDENTIFY.equals(payload.getOp()) ? identifyLimiter : outboundLimiter;
        return Mono.defer(() -> Mono.delay(Duration.ofMillis(calculateDelayMillis(limiter)), Schedulers.single()))
                .map(tick -> limiter.tryConsume(1))
                .flatMap(consumed -> {
                    if (!consumed) {
                        return Mono.error(new RuntimeException());
                    }
                    return Mono.just(payload);
                })
                .retry();
    }

    private long calculateDelayMillis(GatewayLimiter limiter) {
        return limiter.delayMillisToConsume(1) + outboundDelayMillis;
    }

    private Publisher<?> toOutboundFrame(GatewayPayload<? extends PayloadData> payload) {
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
        mainLog.debug("Triggering close sequence");
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
        mainLog.warn("Triggering error sequence ({})", error.toString());
        if (!completionNotifier.isTerminated()) {
            if (error instanceof CloseException) {
                completionNotifier.onError(error);
            } else {
                completionNotifier.onError(new CloseException(new CloseStatus(1006, error.toString()), error));
            }
        }
    }
}
