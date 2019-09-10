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

import discord4j.common.GitProperties;
import discord4j.common.ResettableInterval;
import discord4j.common.close.CloseException;
import discord4j.common.close.CloseStatus;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.Heartbeat;
import discord4j.gateway.json.Opcode;
import discord4j.gateway.json.dispatch.Dispatch;
import discord4j.gateway.json.dispatch.Ready;
import discord4j.gateway.json.dispatch.Resumed;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.retry.GatewayStateChange;
import discord4j.gateway.retry.PartialDisconnectException;
import discord4j.gateway.retry.ReconnectOptions;
import discord4j.gateway.retry.RetryContext;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import reactor.core.publisher.*;
import reactor.core.scheduler.Schedulers;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClient;
import reactor.retry.Retry;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.function.Tuples;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static discord4j.common.LogUtil.format;
import static io.netty.handler.codec.http.HttpHeaderNames.USER_AGENT;

/**
 * Represents a Discord WebSocket client, called Gateway, implementing its lifecycle.
 * <p>
 * Keeps track of a single websocket session by wrapping an instance of {@link DiscordWebSocketHandler} each time a
 * new WebSocket connection to Discord is made, therefore only one instance of this class is enough to
 * handle the lifecycle of the Gateway operations, that could span multiple WebSocket sessions over time.
 * <p>
 * Provides automatic reconnecting through a configurable retry policy, allows consumers to receive inbound events
 * through {@link #dispatch()}, mapped payloads through {@link #receiver()} and allows a producer to
 * submit events through {@link #sender()}.
 * <p>
 * Provides sending raw {@link ByteBuf} payloads through {@link #sendBuffer(Publisher)} and receiving raw
 * {@link ByteBuf} payloads mapped in-flight using a specified mapper using {@link #receiver(Function)}.
 */
public class DefaultGatewayClient implements GatewayClient {

    // basic properties
    private final HttpClient httpClient;
    private final PayloadReader payloadReader;
    private final PayloadWriter payloadWriter;
    private final ReconnectOptions reconnectOptions;
    private final RetryContext retryContext;
    private final IdentifyOptions identifyOptions;
    private final String token;
    private final GatewayObserver observer;
    private final PayloadTransformer identifyLimiter;

    // reactive pipelines
    private final EmitterProcessor<ByteBuf> receiver = EmitterProcessor.create(false);
    private final EmitterProcessor<ByteBuf> sender = EmitterProcessor.create(false);
    private final EmitterProcessor<Dispatch> dispatch = EmitterProcessor.create(false);
    private final EmitterProcessor<GatewayPayload<?>> outbound = EmitterProcessor.create(false);
    private final EmitterProcessor<GatewayPayload<Heartbeat>> heartbeats = EmitterProcessor.create(false);
    private final FluxSink<ByteBuf> receiverSink;
    private final FluxSink<ByteBuf> senderSink;
    private final FluxSink<Dispatch> dispatchSink;
    private final FluxSink<GatewayPayload<?>> outboundSink;
    private final FluxSink<GatewayPayload<Heartbeat>> heartbeatSink;

    // mutable state
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean resumable = new AtomicBoolean(true);
    private final AtomicInteger sequence = new AtomicInteger(0);
    private final AtomicReference<String> sessionId = new AtomicReference<>("");
    private final AtomicLong lastSent = new AtomicLong(0);
    private final AtomicLong lastAck = new AtomicLong(0);
    private final AtomicLong responseTime = new AtomicLong(0);
    private final ResettableInterval heartbeat = new ResettableInterval();
    private volatile MonoProcessor<Void> disconnectNotifier;
    private volatile MonoProcessor<CloseStatus> closeTrigger;

    /**
     * Initializes a new GatewayClient.
     *
     * @param options the {@link GatewayOptions} to configure this client
     */
    public DefaultGatewayClient(GatewayOptions options) {
        this.token = Objects.requireNonNull(options.getToken());
        this.httpClient = Objects.requireNonNull(options.getHttpClient());
        this.payloadReader = Objects.requireNonNull(options.getPayloadReader());
        this.payloadWriter = Objects.requireNonNull(options.getPayloadWriter());
        this.reconnectOptions = options.getReconnectOptions();
        this.retryContext = new RetryContext(reconnectOptions.getFirstBackoff(),
                reconnectOptions.getMaxBackoffInterval());
        this.identifyOptions = Objects.requireNonNull(options.getIdentifyOptions());
        this.observer = options.getInitialObserver();
        this.identifyLimiter = Objects.requireNonNull(options.getIdentifyLimiter());
        // TODO: consider exposing OverflowStrategy to GatewayOptions
        this.receiverSink = receiver.sink(FluxSink.OverflowStrategy.ERROR);
        this.senderSink = sender.sink(FluxSink.OverflowStrategy.ERROR);
        this.dispatchSink = dispatch.sink(FluxSink.OverflowStrategy.BUFFER);
        this.outboundSink = outbound.sink(FluxSink.OverflowStrategy.ERROR);
        this.heartbeatSink = heartbeats.sink(FluxSink.OverflowStrategy.ERROR);
    }

    @Override
    public Mono<Void> execute(String gatewayUrl) {
        return Mono.defer(Mono::subscriberContext)
                .flatMap(context -> {
                    disconnectNotifier = MonoProcessor.create();
                    closeTrigger = MonoProcessor.create();
                    lastAck.set(0);
                    lastSent.set(0);

                    MonoProcessor<Void> ping = MonoProcessor.create();

                    // Setup the sending logic from multiple sources into one merged Flux
                    Flux<ByteBuf> identifyFlux = outbound.filter(payload -> Opcode.IDENTIFY.equals(payload.getOp()))
                            .delayUntil(payload -> ping)
                            .flatMap(payload -> Flux.from(payloadWriter.write(payload)))
                            .map(buf -> Tuples.of((GatewayClient) this, buf))
                            .transform(identifyLimiter);
                    Flux<ByteBuf> payloadFlux = outbound.filter(payload -> !Opcode.IDENTIFY.equals(payload.getOp()))
                            .doOnEach(s -> outboundLog.debug(format(context, s.toString())))
                            .flatMap(payload -> Flux.from(payloadWriter.write(payload)))
                            .transform(buf -> Flux.merge(buf, sender))
                            .map(buf -> Tuples.of((GatewayClient) this, buf))
                            .transform(new PoolingTransformer(outboundLimiterCapacity(), Duration.ofSeconds(60)));
                    Flux<ByteBuf> heartbeatFlux =
                            heartbeats.flatMap(payload -> Flux.from(payloadWriter.write(payload)));
                    Flux<ByteBuf> outFlux = Flux.merge(heartbeatFlux, identifyFlux, payloadFlux)
                            .doOnNext(buf -> trace(senderLog, buf));

                    int shard = identifyOptions.getShardIndex();
                    DiscordWebSocketHandler handler = new DiscordWebSocketHandler(receiverSink, outFlux, closeTrigger
                            , shard);

                    Integer resumeSequence = identifyOptions.getResumeSequence();
                    if (resumeSequence != null && resumeSequence > 0) {
                        this.sequence.set(identifyOptions.getResumeSequence());
                        this.sessionId.set(identifyOptions.getResumeSessionId());
                    } else {
                        resumable.set(false);
                    }

                    Mono<Void> readyHandler = dispatch.filter(DefaultGatewayClient::isReadyOrResume)
                            .flatMap(event -> {
                                connected.compareAndSet(false, true);
                                ConnectionObserver.State state;
                                if (retryContext.getResetCount() == 0) {
                                    log.info(format(context, "Connected to Gateway"));
                                    dispatchSink.next(GatewayStateChange.connected());
                                    state = GatewayObserver.CONNECTED;
                                } else {
                                    log.info(format(context, "Reconnected to Gateway"));
                                    dispatchSink.next(GatewayStateChange.retrySucceeded(retryContext.getAttempts()));
                                    state = GatewayObserver.RETRY_SUCCEEDED;
                                }
                                retryContext.reset();
                                identifyOptions.setResumeSessionId(sessionId.get());
                                resumable.set(true);
                                notifyObserver(state, identifyOptions);
                                return Mono.just(event);
                            })
                            .then();

                    // Subscribe the receiver to process and transform the inbound payloads into Dispatch events
                    Flux<GatewayPayload<?>> receiverFlux = receiver.doOnNext(buf -> trace(receiverLog, buf))
                            .flatMap(payloadReader::read);

                    Mono<Void> receiverFuture =
                            receiverFlux.filter(payload -> !Opcode.HEARTBEAT_ACK.equals(payload.getOp()))
                                    .doOnEach(s -> inboundLog.debug(format(context, s.toString())))
                                    .map(this::updateSequence)
                                    .map(payload -> new PayloadContext<>(payload, handler, this, context))
                                    .doOnNext(PayloadHandlers::handle)
                                    .then();

                    Mono<Void> ackFuture = receiverFlux.filter(payload -> Opcode.HEARTBEAT_ACK.equals(payload.getOp()))
                            .map(payload -> new PayloadContext<>(payload, handler, this, context))
                            .publishOn(Schedulers.elastic())
                            .doOnNext(PayloadHandlers::handle)
                            .doOnNext(ctx -> ping.onComplete())
                            .then();

                    // Subscribe the handler's outbound exchange with our outgoing signals
                    // routing completion signals to close the gateway
                    Mono<Void> senderFuture = outbound.doOnComplete(handler::close)
                            .doOnNext(payload -> {
                                if (Opcode.RECONNECT.equals(payload.getOp())) {
                                    handler.error(new RuntimeException("Reconnecting due to user action"));
                                }
                            })
                            .then();

                    // Create the heartbeat loop, and subscribe it using the sender sink
                    Mono<Void> heartbeatHandler = heartbeat.ticks()
                            .flatMap(t -> {
                                long now = System.nanoTime();
                                lastAck.compareAndSet(0, now);
                                long delay = now - lastAck.get();
                                if (lastSent.get() - lastAck.get() > 0) {
                                    log.warn(format(context, "Missing heartbeat ACK for {}"), Duration.ofNanos(delay));
                                    handler.error(new RuntimeException("Reconnecting due to zombie or failed " +
                                            "connection"));
                                    return Mono.empty();
                                } else {
                                    log.debug(format(context, "Sending heartbeat {} after last ACK"),
                                            Duration.ofNanos(delay));
                                    lastSent.set(now);
                                    return Mono.just(GatewayPayload.heartbeat(new Heartbeat(sequence.get())));
                                }
                            })
                            .doOnNext(heartbeatSink::next)
                            .then();

                    Mono<Void> httpFuture = httpClient
                            .headers(headers -> headers.add(USER_AGENT, initUserAgent()))
                            .observe(getObserver())
                            .websocket(Integer.MAX_VALUE)
                            .uri(gatewayUrl)
                            .handle(handler::handle)
                            .doOnTerminate(heartbeat::stop)
                            .then();

                    return Mono.zip(httpFuture, readyHandler, receiverFuture, ackFuture, senderFuture, heartbeatHandler)
                            .doOnError(logReconnectReason())
                            .then();
                })
                .retryWhen(retryFactory())
                .doOnCancel(() -> closeTrigger.onNext(CloseStatus.NORMAL_CLOSE))
                .then(Mono.defer(() -> disconnectNotifier));
    }

    private String initUserAgent() {
        final Properties properties = GitProperties.getProperties();
        final String version = properties.getProperty(GitProperties.APPLICATION_VERSION, "3");
        final String url = properties.getProperty(GitProperties.APPLICATION_URL, "https://discord4j.com");
        return "DiscordBot(" + url + ", " + version + ")";
    }

    private void trace(Logger log, ByteBuf buf) {
        if (log.isTraceEnabled()) {
            log.trace(buf.toString(StandardCharsets.UTF_8)
                    .replaceAll("(\"token\": ?\")([A-Za-z0-9.-]*)(\")", "$1hunter2$3"));
        }
    }

    private static boolean isReadyOrResume(Dispatch d) {
        return Ready.class.isAssignableFrom(d.getClass()) || Resumed.class.isAssignableFrom(d.getClass());
    }

    private GatewayPayload<?> updateSequence(GatewayPayload<?> payload) {
        if (payload.getSequence() != null) {
            sequence.set(payload.getSequence());
            identifyOptions.setResumeSequence(sequence.get());
            notifyObserver(GatewayObserver.SEQUENCE, identifyOptions);
        }
        return payload;
    }

    private Retry<RetryContext> retryFactory() {
        return Retry.<RetryContext>onlyIf(t -> isRetryable(t.exception()))
                .withApplicationContext(retryContext)
                .withBackoffScheduler(reconnectOptions.getBackoffScheduler())
                .backoff(reconnectOptions.getBackoff())
                .jitter(reconnectOptions.getJitter())
                .retryMax(reconnectOptions.getMaxRetries())
                .doOnRetry(context -> {
                    connected.compareAndSet(true, false);
                    int attempt = context.applicationContext().getAttempts();
                    long backoff = context.backoff().toMillis();
                    log.info("Retry attempt {} in {} ms", attempt, backoff);
                    if (attempt == 1) {
                        dispatchSink.next(GatewayStateChange.retryStarted(Duration.ofMillis(backoff)));
                        if (!resumable.get() || !isResumableError(context.exception())) {
                            resumable.compareAndSet(true, false);
                            notifyObserver(GatewayObserver.RETRY_STARTED, identifyOptions);
                        } else {
                            notifyObserver(GatewayObserver.RETRY_RESUME_STARTED, identifyOptions);
                        }
                    } else {
                        dispatchSink.next(GatewayStateChange.retryFailed(attempt - 1,
                                Duration.ofMillis(backoff)));
                        // TODO: add attempt/backoff values to GatewayObserver
                        notifyObserver(GatewayObserver.RETRY_FAILED, identifyOptions);
                        resumable.set(false);
                    }
                    context.applicationContext().next();
                });
    }

    private boolean isRetryable(Throwable t) {
        if (t instanceof CloseException) {
            CloseException closeException = (CloseException) t;
            return closeException.getCode() != 4004;
        }
        return !(t instanceof PartialDisconnectException);
    }

    private boolean isResumableError(Throwable t) {
        if (t instanceof CloseException) {
            CloseException closeException = (CloseException) t;
            return closeException.getCode() < 4000;
        }
        return true;
    }

    private Consumer<Throwable> logReconnectReason() {
        return t -> {
            if ((t instanceof CloseException && isResumableError(t)) || t instanceof PartialDisconnectException) {
                log.error("Gateway client error: {}", t.toString());
            } else {
                log.error("Gateway client error", t);
            }
        };
    }

    private ConnectionObserver getObserver() {
        return (connection, newState) -> {
            log.debug("{} {}", newState, connection);
            if (closeTrigger.isTerminated() && (newState == ConnectionObserver.State.RELEASED
                    || newState == ConnectionObserver.State.DISCONNECTING)) {
                log.info("Disconnected from Gateway");
                retryContext.clear();
                connected.compareAndSet(true, false);
                lastSent.set(0);
                lastAck.set(0);
                responseTime.set(0);
                dispatchSink.next(GatewayStateChange.disconnected());
                if (closeTrigger.isError()) {
                    notifyObserver(GatewayObserver.DISCONNECTED_RESUME, identifyOptions);
                } else {
                    resumable.set(false);
                    sequence.set(0);
                    sessionId.set("");
                    notifyObserver(GatewayObserver.DISCONNECTED, identifyOptions);
                }
                disconnectNotifier.onComplete();
            }
            notifyObserver(newState, identifyOptions);
        };
    }

    private void notifyObserver(ConnectionObserver.State state, IdentifyOptions options) {
        observer.onStateChange(state, options);
    }

    @Override
    public Mono<Void> close(boolean allowResume) {
        return Mono.defer(() -> {
            if (closeTrigger == null || disconnectNotifier == null) {
                return Mono.error(new IllegalStateException("Gateway client is not active!"));
            }
            if (allowResume) {
                closeTrigger.onError(new PartialDisconnectException());
            } else {
                closeTrigger.onNext(CloseStatus.NORMAL_CLOSE);
            }
            return disconnectNotifier;
        });
    }

    @Override
    public Flux<Dispatch> dispatch() {
        return dispatch;
    }

    @Override
    public Flux<GatewayPayload<?>> receiver() {
        return receiver.flatMap(payloadReader::read);
    }

    @Override
    public <T> Flux<T> receiver(Function<ByteBuf, Publisher<? extends T>> mapper) {
        return receiver.flatMap(mapper);
    }

    @Override
    public FluxSink<GatewayPayload<?>> sender() {
        return outboundSink;
    }

    @Override
    public Mono<Void> sendBuffer(Publisher<ByteBuf> publisher) {
        return Flux.from(publisher).doOnNext(senderSink::next).then();
    }

    @Override
    public String getSessionId() {
        return sessionId.get();
    }

    @Override
    public int getSequence() {
        return sequence.get();
    }

    @Override
    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public long getResponseTime() {
        return TimeUnit.NANOSECONDS.toMillis(responseTime.get());
    }

    // TODO: getResponseTime for 3.1
    Duration getResponseTimeDuration() {
        return Duration.ofNanos(responseTime.get());
    }

    /////////////////////////////////
    // Methods for PayloadHandlers //
    /////////////////////////////////

    void ackHeartbeat() {
        responseTime.set(lastAck.updateAndGet(x -> System.nanoTime()) - lastSent.get());
    }

    ////////////////////////////////
    // Fields for PayloadHandlers //
    ////////////////////////////////

    /**
     * Obtains the FluxSink to send Dispatch events towards GatewayClient's users.
     *
     * @return a {@link FluxSink} for {@link Dispatch}
     * objects
     */
    FluxSink<Dispatch> dispatchSink() {
        return dispatchSink;
    }

    /**
     * Gets the atomic reference for the current heartbeat sequence.
     *
     * @return an AtomicInteger representing the current gateway sequence
     */
    AtomicInteger sequence() {
        return sequence;
    }

    /**
     * Gets the atomic reference for the current session ID.
     *
     * @return an AtomicReference of the String representing the current session ID
     */
    AtomicReference<String> sessionId() {
        return sessionId;
    }

    /**
     * Gets the heartbeat manager bound to this GatewayClient.
     *
     * @return a {@link ResettableInterval} to manipulate heartbeat operations
     */
    ResettableInterval heartbeat() {
        return heartbeat;
    }

    /**
     * Gets the token used to connect to the gateway.
     *
     * @return a token String
     */
    String token() {
        return token;
    }

    /**
     * An boolean value indicating if this client will attempt to RESUME.
     *
     * @return an AtomicBoolean representing resume capabilities
     */
    AtomicBoolean resumable() {
        return resumable;
    }

    /**
     * Gets the configuration object for gateway identifying procedure.
     *
     * @return an IdentifyOptions configuration object
     */
    IdentifyOptions identifyOptions() {
        return identifyOptions;
    }

    // Initializers to customize internal outbound rate-limiter

    /**
     * JVM property that allows modifying the number of outbound payloads permitted before activating the
     * rate-limiter and delaying every following payload for 60 seconds. Default value: 115 permits
     */
    private static final String OUTBOUND_CAPACITY_PROPERTY = "discord4j.gateway.outbound.capacity";

    private int outboundLimiterCapacity() {
        String capacityValue = System.getProperty(OUTBOUND_CAPACITY_PROPERTY);
        if (capacityValue != null) {
            try {
                int capacity = Integer.parseInt(capacityValue);
                log.info("Overriding default outbound limiter capacity: {}", capacity);
            } catch (NumberFormatException e) {
                log.warn("Invalid custom outbound limiter capacity: {}", capacityValue);
            }
        }
        return 115;
    }

    private static final Logger log = Loggers.getLogger("discord4j.gateway");
    private static final Logger senderLog = Loggers.getLogger("discord4j.gateway.sender");
    private static final Logger receiverLog = Loggers.getLogger("discord4j.gateway.receiver");
    private static final Logger outboundLog = Loggers.getLogger("discord4j.gateway.outbound");
    private static final Logger inboundLog = Loggers.getLogger("discord4j.gateway.inbound");
}
