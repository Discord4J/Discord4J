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
import discord4j.common.LogUtil;
import discord4j.common.ResettableInterval;
import discord4j.common.close.CloseException;
import discord4j.common.close.CloseStatus;
import discord4j.common.close.DisconnectBehavior;
import discord4j.common.operator.RateLimitOperator;
import discord4j.common.retry.ReconnectContext;
import discord4j.common.retry.ReconnectOptions;
import discord4j.discordjson.json.gateway.*;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.limiter.PayloadTransformer;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.retry.*;
import io.netty.buffer.ByteBuf;
import io.netty.util.IllegalReferenceCountException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.*;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.WebsocketClientSpec;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.context.Context;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static discord4j.common.LogUtil.format;
import static io.netty.handler.codec.http.HttpHeaderNames.USER_AGENT;
import static reactor.function.TupleUtils.consumer;

/**
 * Represents a Discord WebSocket client, called Gateway, implementing its lifecycle.
 * <p>
 * Keeps track of a single websocket session by wrapping an instance of {@link GatewayWebsocketHandler} each time a
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

    private static final Logger log = Loggers.getLogger(DefaultGatewayClient.class);
    private static final Logger senderLog = Loggers.getLogger("discord4j.gateway.protocol.sender");
    private static final Logger receiverLog = Loggers.getLogger("discord4j.gateway.protocol.receiver");

    // basic properties
    private final GatewayReactorResources reactorResources;
    private final PayloadReader payloadReader;
    private final PayloadWriter payloadWriter;
    private final ReconnectOptions reconnectOptions;
    private final ReconnectContext reconnectContext;
    private final IdentifyOptions identifyOptions;
    private final String token;
    private final GatewayObserver observer;
    private final PayloadTransformer identifyLimiter;
    private final ResettableInterval heartbeat;
    private final int maxMissedHeartbeatAck;

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

    private final ReplayProcessor<GatewayConnection.State> state;
    private final FluxSink<GatewayConnection.State> stateChanges;

    // mutable state, modified here and at PayloadHandlers
    private final AtomicInteger sequence = new AtomicInteger(0);
    private final AtomicReference<String> sessionId = new AtomicReference<>("");
    private final AtomicLong lastSent = new AtomicLong(0);
    private final AtomicLong lastAck = new AtomicLong(0);
    private final AtomicInteger missedAck = new AtomicInteger(0);
    private volatile long responseTime = 0;
    private volatile MonoProcessor<CloseStatus> disconnectNotifier;
    private volatile GatewayWebsocketHandler sessionHandler;

    /**
     * Initializes a new GatewayClient.
     *
     * @param options the {@link GatewayOptions} to configure this client
     */
    public DefaultGatewayClient(GatewayOptions options) {
        this.token = Objects.requireNonNull(options.getToken());
        this.reactorResources = Objects.requireNonNull(options.getReactorResources());
        this.payloadReader = Objects.requireNonNull(options.getPayloadReader());
        this.payloadWriter = Objects.requireNonNull(options.getPayloadWriter());
        this.reconnectOptions = options.getReconnectOptions();
        this.reconnectContext = new ReconnectContext(
                this.reconnectOptions.getFirstBackoff(), this.reconnectOptions.getMaxBackoffInterval());
        this.identifyOptions = Objects.requireNonNull(options.getIdentifyOptions());
        this.observer = options.getInitialObserver();
        this.identifyLimiter = Objects.requireNonNull(options.getIdentifyLimiter());
        this.maxMissedHeartbeatAck = Math.max(0, options.getMaxMissedHeartbeatAck());
        // TODO: consider exposing OverflowStrategy to GatewayOptions
        this.receiverSink = receiver.sink(FluxSink.OverflowStrategy.BUFFER);
        this.senderSink = sender.sink(FluxSink.OverflowStrategy.ERROR);
        this.dispatchSink = dispatch.sink(FluxSink.OverflowStrategy.BUFFER);
        this.outboundSink = outbound.sink(FluxSink.OverflowStrategy.ERROR);
        this.heartbeatSink = heartbeats.sink(FluxSink.OverflowStrategy.ERROR);
        this.heartbeat = new ResettableInterval(this.reactorResources.getTimerTaskScheduler());

        SessionInfo resumeSession = this.identifyOptions.getResumeSession().orElse(null);
        if (resumeSession != null) {
            this.sequence.set(resumeSession.getSequence());
            this.sessionId.set(resumeSession.getId());
            this.state = ReplayProcessor.cacheLastOrDefault(GatewayConnection.State.START_RESUMING);
        } else {
            this.state = ReplayProcessor.cacheLastOrDefault(GatewayConnection.State.START_IDENTIFYING);
        }
        this.stateChanges = state.sink(FluxSink.OverflowStrategy.LATEST);
    }

    @Override
    public Mono<Void> execute(String gatewayUrl) {
        return Mono.deferWithContext(
                context -> {
                    disconnectNotifier = MonoProcessor.create();
                    lastAck.set(0);
                    lastSent.set(0);
                    missedAck.set(0);

                    MonoProcessor<Void> ping = MonoProcessor.create();

                    // Setup the sending logic from multiple sources into one merged Flux
                    Flux<ByteBuf> heartbeatFlux =
                            heartbeats.flatMap(payload -> Flux.from(payloadWriter.write(payload)));
                    Flux<ByteBuf> identifyFlux = outbound.filter(payload -> Opcode.IDENTIFY.equals(payload.getOp()))
                            .delayUntil(payload -> ping)
                            .flatMap(payload -> Flux.from(payloadWriter.write(payload)))
                            .transform(identifyLimiter);
                    Flux<ByteBuf> payloadFlux = outbound.filter(payload -> !Opcode.IDENTIFY.equals(payload.getOp()))
                            .flatMap(payload -> Flux.from(payloadWriter.write(payload)))
                            .transform(buf -> Flux.merge(buf, sender))
                            .transform(new RateLimitOperator<>(outboundLimiterCapacity(), Duration.ofSeconds(60),
                                    reactorResources.getTimerTaskScheduler(),
                                    reactorResources.getPayloadSenderScheduler()));
                    Flux<ByteBuf> outFlux = Flux.merge(heartbeatFlux, identifyFlux, payloadFlux)
                            .doOnNext(buf -> logPayload(senderLog, context, buf));

                    sessionHandler = new GatewayWebsocketHandler(receiverSink, outFlux, context);

                    Mono<Void> readyHandler = dispatch.filter(DefaultGatewayClient::isReadyOrResumed)
                            .zipWith(state.next().repeat())
                            .doOnNext(consumer((event, currentState) -> {
                                ConnectionObserver.State observerState;
                                if (currentState == GatewayConnection.State.START_IDENTIFYING
                                        || currentState == GatewayConnection.State.START_RESUMING) {
                                    log.info(format(context, "Connected to Gateway"));
                                    dispatchSink.next(GatewayStateChange.connected());
                                    observerState = GatewayObserver.CONNECTED;
                                } else {
                                    log.info(format(context, "Reconnected to Gateway"));
                                    dispatchSink.next(GatewayStateChange.retrySucceeded(reconnectContext.getAttempts()));
                                    observerState = GatewayObserver.RETRY_SUCCEEDED;
                                }

                                reconnectContext.reset();
                                stateChanges.next(GatewayConnection.State.CONNECTED);
                                notifyObserver(observerState);
                            }))
                            .then();

                    // Subscribe the receiver to process and transform the inbound payloads into Dispatch events
                    Mono<Void> receiverFuture = receiver.map(ByteBuf::retain)
                            .doOnNext(buf -> logPayload(receiverLog, context, buf))
                            .flatMap(payloadReader::read)
                            .doOnDiscard(ByteBuf.class, DefaultGatewayClient::safeRelease)
                            .doOnNext(payload -> {
                                if (Opcode.HEARTBEAT_ACK.equals(payload.getOp())) {
                                    ping.onComplete();
                                }
                            })
                            .map(this::updateSequence)
                            .map(payload -> new PayloadContext<>(payload, sessionHandler, this, context))
                            .flatMap(PayloadHandlers::handle)
                            .then();

                    // Subscribe the handler's outbound exchange with our outbound signals
                    // routing completion signals to close the gateway
                    Mono<Void> senderFuture = outbound.doOnComplete(sessionHandler::close)
                            .doOnNext(payload -> {
                                if (Opcode.RECONNECT.equals(payload.getOp())) {
                                    sessionHandler.error(
                                            new GatewayException(context, "Reconnecting due to user action"));
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
                                    if (missedAck.incrementAndGet() > maxMissedHeartbeatAck) {
                                        log.warn(format(context, "Missing heartbeat ACK for {} (tick: {}, seq: {})"),
                                                Duration.ofNanos(delay), t, sequence.get());
                                        sessionHandler.error(new GatewayException(context,
                                                "Reconnecting due to zombie or failed connection"));
                                        return Mono.empty();
                                    }
                                }
                                log.debug(format(context, "Sending heartbeat {} after last ACK"),
                                        Duration.ofNanos(delay));
                                lastSent.set(now);
                                return Mono.just(GatewayPayload.heartbeat(ImmutableHeartbeat.of(sequence.get())));
                            })
                            .doOnNext(heartbeatSink::next)
                            .then();

                    Mono<Void> httpFuture = reactorResources.getHttpClient()
                            .headers(headers -> headers.add(USER_AGENT, initUserAgent()))
                            .observe(getObserver(context))
                            .websocket(WebsocketClientSpec.builder()
                                    .maxFramePayloadLength(Integer.MAX_VALUE)
                                    .build())
                            .uri(gatewayUrl)
                            .handle(sessionHandler::handle)
                            .subscriberContext(LogUtil.clearContext())
                            .flatMap(t2 -> handleClose(t2.getT1(), t2.getT2()))
                            .then();

                    return Mono.zip(httpFuture, readyHandler, receiverFuture, senderFuture, heartbeatHandler)
                            .doOnError(t -> {
                                if (t instanceof ReconnectException) {
                                    log.info(format(context, "{}"), t.getMessage());
                                } else {
                                    if (log.isTraceEnabled()) {
                                        log.error(format(context, "Gateway client error"), t);
                                    } else {
                                        log.error(format(context, "{}"), t.toString());
                                    }
                                }
                            })
                            .doOnTerminate(heartbeat::stop)
                            .doOnCancel(() -> sessionHandler.close())
                            .then();
                })
                .subscriberContext(ctx -> ctx.put(LogUtil.KEY_SHARD_ID, identifyOptions.getShardInfo().getIndex()))
                .retryWhen(retryFactory())
                .then(Mono.defer(() -> disconnectNotifier.then()))
                .doOnSubscribe(s -> {
                    if (disconnectNotifier != null) {
                        throw new IllegalStateException("execute can only be subscribed once");
                    }
                });
    }

    private String initUserAgent() {
        final Properties properties = GitProperties.getProperties();
        final String version = properties.getProperty(GitProperties.APPLICATION_VERSION, "3");
        final String url = properties.getProperty(GitProperties.APPLICATION_URL, "https://discord4j.com");
        return "DiscordBot(" + url + ", " + version + ")";
    }

    private void logPayload(Logger logger, Context context, ByteBuf buf) {
        logger.trace(format(context, buf.toString(StandardCharsets.UTF_8)
                .replaceAll("(\"token\": ?\")([A-Za-z0-9._-]*)(\")", "$1hunter2$3")));
    }

    private static boolean isReadyOrResumed(Dispatch d) {
        return Ready.class.isAssignableFrom(d.getClass()) || Resumed.class.isAssignableFrom(d.getClass());
    }

    private GatewayPayload<?> updateSequence(GatewayPayload<?> payload) {
        if (payload.getSequence() != null) {
            sequence.set(payload.getSequence());
            notifyObserver(GatewayObserver.SEQUENCE);
        }
        return payload;
    }

    private Retry retryFactory() {
        return GatewayRetrySpec.create(reconnectOptions, reconnectContext)
                .doBeforeRetry(retry -> {
                    stateChanges.next(retry.nextState());
                    long attempt = retry.iteration();
                    Duration backoff = retry.nextBackoff();
                    log.debug(format(getContextFromException(retry.failure()),
                            "{} in {} (attempts: {})"), retry.nextState(), backoff, attempt);
                    if (retry.iteration() == 1) {
                        if (retry.nextState() == GatewayConnection.State.RESUMING) {
                            dispatchSink.next(GatewayStateChange.retryStarted(backoff));
                            notifyObserver(GatewayObserver.RETRY_STARTED);
                        } else {
                            dispatchSink.next(GatewayStateChange.retryStartedResume(backoff));
                            notifyObserver(GatewayObserver.RETRY_RESUME_STARTED);
                        }
                    } else {
                        dispatchSink.next(GatewayStateChange.retryFailed(attempt - 1, backoff));
                        notifyObserver(GatewayObserver.RETRY_FAILED);
                    }
                    if (retry.nextState() == GatewayConnection.State.RECONNECTING) {
                        dispatchSink.next(GatewayStateChange.sessionInvalidated());
                    }
                });
    }

    private Context getContextFromException(Throwable t) {
        if (t instanceof CloseException) {
            return ((CloseException) t).getContext();
        }
        if (t instanceof GatewayException) {
            return ((GatewayException) t).getContext();
        }
        return Context.empty();
    }

    private Mono<CloseStatus> handleClose(DisconnectBehavior sourceBehavior, CloseStatus closeStatus) {
        return Mono.deferWithContext(ctx -> {
            DisconnectBehavior behavior;
            if (GatewayRetrySpec.NON_RETRYABLE_STATUS_CODES.contains(closeStatus.getCode())) {
                // non-retryable close codes are non-transient errors therefore stopping is the only choice
                behavior = DisconnectBehavior.stop(sourceBehavior.getCause());
            } else {
                behavior = sourceBehavior;
            }
            log.debug(format(ctx, "Closing and {} with status {}"), behavior, closeStatus);
            stateChanges.next(GatewayConnection.State.DISCONNECTING);
            heartbeat.stop();

            if (behavior.getAction() == DisconnectBehavior.Action.STOP_ABRUPTLY) {
                dispatchSink.next(GatewayStateChange.disconnectedResume());
                notifyObserver(GatewayObserver.DISCONNECTED_RESUME);
            } else if (behavior.getAction() == DisconnectBehavior.Action.STOP) {
                dispatchSink.next(GatewayStateChange.disconnected(sourceBehavior, closeStatus));
                sequence.set(0);
                sessionId.set("");
                notifyObserver(GatewayObserver.DISCONNECTED);
            }

            switch (behavior.getAction()) {
                case STOP_ABRUPTLY:
                case STOP:
                    reconnectContext.clear();
                    responseTime = 0;
                    lastSent.set(0);
                    lastAck.set(0);
                    stateChanges.next(GatewayConnection.State.DISCONNECTED);
                    if (behavior.getCause() != null) {
                        return Mono.just(new CloseException(closeStatus, ctx, behavior.getCause()))
                                .flatMap(ex -> {
                                    disconnectNotifier.onError(ex);
                                    return Mono.error(ex);
                                });
                    }
                    return Mono.just(closeStatus).doOnNext(status -> disconnectNotifier.onNext(closeStatus));
                case RETRY_ABRUPTLY:
                case RETRY:
                default:
                    return Mono.error(new CloseException(closeStatus, ctx, behavior.getCause()));
            }
        });
    }

    private ConnectionObserver getObserver(Context context) {
        return (connection, newState) -> {
            log.debug(format(context, "{} {}"), newState, connection);
            notifyObserver(newState);
        };
    }

    private void notifyObserver(ConnectionObserver.State state) {
        observer.onStateChange(state, this);
    }

    @Override
    public Mono<Void> close(boolean allowResume) {
        return Mono.defer(() -> {
            if (sessionHandler == null || disconnectNotifier == null) {
                return Mono.error(new IllegalStateException("Gateway client is not active!"));
            }
            if (!disconnectNotifier.isTerminated()) {
                if (allowResume) {
                    sessionHandler.close(DisconnectBehavior.stopAbruptly(null));
                } else {
                    sessionHandler.close(DisconnectBehavior.stop(null));
                }
            }
            return disconnectNotifier.then();
        });
    }

    @Override
    public Flux<Dispatch> dispatch() {
        return dispatch;
    }

    @Override
    public Flux<GatewayPayload<?>> receiver() {
        return receiver(payloadReader::read);
    }

    @Override
    public <T> Flux<T> receiver(Function<ByteBuf, Publisher<? extends T>> mapper) {
        return receiver.map(ByteBuf::retainedDuplicate)
                .doOnDiscard(ByteBuf.class, DefaultGatewayClient::safeRelease)
                .flatMap(mapper);
    }

    private static void safeRelease(ByteBuf buf) {
        if (buf.refCnt() > 0) {
            try {
                buf.release();
            } catch (IllegalReferenceCountException e) {
                if (log.isDebugEnabled()) {
                    log.debug("", e);
                }
            }
        }
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
    public int getShardCount() {
        return identifyOptions.getShardInfo().getCount();
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
    public Flux<GatewayConnection.State> stateEvents() {
        return state;
    }

    @Override
    public Mono<Boolean> isConnected() {
        return state.next()
                .filter(s -> s == GatewayConnection.State.CONNECTED)
                .hasElement()
                .defaultIfEmpty(false);
    }

    @Override
    public Duration getResponseTime() {
        return Duration.ofNanos(responseTime);
    }

    /////////////////////////////////
    // Methods for PayloadHandlers //
    /////////////////////////////////

    void ackHeartbeat() {
        responseTime = lastAck.updateAndGet(x -> System.nanoTime()) - lastSent.get();
        missedAck.set(0);
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
     * Gets the configuration object for gateway identifying procedure.
     *
     * @return an IdentifyOptions configuration object
     */
    IdentifyOptions identifyOptions() {
        return identifyOptions;
    }

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
                return capacity;
            } catch (NumberFormatException e) {
                log.warn("Invalid custom outbound limiter capacity: {}", capacityValue);
            }
        }
        return 115;
    }
}
