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
import discord4j.common.sinks.EmissionStrategy;
import discord4j.discordjson.json.gateway.*;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.limiter.PayloadTransformer;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.retry.*;
import io.netty.buffer.ByteBuf;
import io.netty.util.IllegalReferenceCountException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;
import reactor.util.context.ContextView;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static discord4j.common.LogUtil.format;
import static io.netty.handler.codec.http.HttpHeaderNames.USER_AGENT;
import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;
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
    private final ResettableInterval heartbeatEmitter;
    private final int maxMissedHeartbeatAck;
    private final boolean unpooled;
    private final EmissionStrategy emissionStrategy;

    private final Map<Opcode<?>, PayloadHandler<?>> handlerMap = new HashMap<>();

    private final HttpClient httpClient;

    /**
     * Payloads coming from the websocket.
     */
    private final Sinks.Many<ByteBuf> receiver;

    /**
     * Payloads that are being sent to the websocket.
     */
    private final Sinks.Many<ByteBuf> sender;

    /**
     * Inbound gateway events from {@code receiver} that are pushed to consumers.
     */
    private final Sinks.Many<Dispatch> dispatch;

    /**
     * Outbound gateway events before they are pushed to {@code sender}.
     */
    private final Sinks.Many<GatewayPayload<?>> outbound;

    /**
     * A companion to {@code outbound} dedicated to collecting Gateway mandatory heartbeats.
     */
    private final Sinks.Many<GatewayPayload<Heartbeat>> heartbeats;

    /**
     * Internal connection state changes are reflected as emissions to this sink.
     */
    private final Sinks.Many<GatewayConnection.State> state;

    // Gateway session state tracking across multiple ws connections
    private final AtomicInteger sequence = new AtomicInteger(0);
    private final AtomicReference<String> sessionId = new AtomicReference<>("");
    private final AtomicLong lastSent = new AtomicLong(0);
    private final AtomicLong lastAck = new AtomicLong(0);
    private final AtomicInteger missedAck = new AtomicInteger(0);
    private volatile long responseTime = 0;

    // References that are changing each time a new ws connection is opened
    private volatile Sinks.One<CloseStatus> disconnectNotifier;
    private volatile GatewayWebsocketHandler sessionHandler;
    private volatile ContextView currentContext;

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
        this.unpooled = options.isUnpooled();
        this.emissionStrategy = options.getEmissionStrategy();

        addHandler(Opcode.DISPATCH, this::handleDispatch);
        addHandler(Opcode.HEARTBEAT, this::handleHeartbeat);
        addHandler(Opcode.RECONNECT, this::handleReconnect);
        addHandler(Opcode.INVALID_SESSION, this::handleInvalidSession);
        addHandler(Opcode.HELLO, this::handleHello);
        addHandler(Opcode.HEARTBEAT_ACK, this::handleHeartbeatAck);

        this.httpClient = initHttpClient();
        this.receiver = newEmitterSink();
        this.sender = newEmitterSink();
        this.dispatch = newEmitterSink();
        this.outbound = newEmitterSink();
        this.heartbeats = newEmitterSink();

        this.heartbeatEmitter = new ResettableInterval(this.reactorResources.getTimerTaskScheduler());

        SessionInfo resumeSession = this.identifyOptions.getResumeSession().orElse(null);
        if (resumeSession != null) {
            this.sequence.set(resumeSession.getSequence());
            this.sessionId.set(resumeSession.getId());
            this.state = Sinks.many().replay().latestOrDefault(GatewayConnection.State.START_RESUMING);
        } else {
            this.state = Sinks.many().replay().latestOrDefault(GatewayConnection.State.START_IDENTIFYING);
        }
    }

    private <T extends PayloadData> void addHandler(Opcode<T> op, PayloadHandler<T> handler) {
        handlerMap.put(op, handler);
    }

    private static <T> Sinks.Many<T> newEmitterSink() {
        return Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
    }

    @Override
    public Mono<Void> execute(String gatewayUrl) {
        return Mono.deferContextual(
                context -> {
                    currentContext = context;
                    disconnectNotifier = Sinks.one();
                    lastAck.set(0);
                    lastSent.set(0);
                    missedAck.set(0);

                    Sinks.Empty<Void> ping = Sinks.empty();

                    // Setup the sending logic from multiple sources into one merged Flux
                    Mono<Void> onConnected = state.asFlux().filter(s -> s == GatewayConnection.State.CONNECTED).next().then();
                    Flux<ByteBuf> heartbeatFlux = heartbeats.asFlux()
                            .flatMap(payload -> Flux.from(payloadWriter.write(payload)));
                    Flux<ByteBuf> identifyFlux = outbound.asFlux()
                            .filter(payload -> Opcode.IDENTIFY.equals(payload.getOp()))
                            .delayUntil(__ -> ping.asMono())
                            .flatMap(payload -> Flux.from(payloadWriter.write(payload)))
                            .transform(identifyLimiter);
                    Flux<ByteBuf> resumeFlux = outbound.asFlux()
                            .filter(payload -> Opcode.RESUME.equals(payload.getOp()))
                            .flatMap(payload -> Flux.from(payloadWriter.write(payload)));
                    Flux<ByteBuf> payloadFlux = outbound.asFlux()
                            .filter(DefaultGatewayClient::isNotStartupPayload)
                            .delayUntil(__ -> onConnected)
                            .flatMap(payload -> Flux.from(payloadWriter.write(payload)))
                            .transform(buf -> Flux.merge(buf, sender.asFlux()))
                            .transform(new RateLimitOperator<>(outboundLimiterCapacity(), Duration.ofSeconds(60),
                                    reactorResources.getTimerTaskScheduler(),
                                    reactorResources.getPayloadSenderScheduler()));
                    Flux<ByteBuf> outFlux = Flux.merge(heartbeatFlux, identifyFlux, resumeFlux, payloadFlux)
                            .doOnNext(buf -> logPayload(senderLog, context, buf))
                            .doOnDiscard(ByteBuf.class, DefaultGatewayClient::safeRelease);

                    sessionHandler = new GatewayWebsocketHandler(receiver, outFlux, context);

                    Mono<Void> readyHandler = dispatch.asFlux()
                            .filter(DefaultGatewayClient::isReadyOrResumed)
                            .zipWith(state.asFlux().next().repeat())
                            .doOnNext(consumer((event, currentState) -> {
                                ConnectionObserver.State observerState;
                                if (currentState == GatewayConnection.State.START_IDENTIFYING
                                        || currentState == GatewayConnection.State.START_RESUMING) {
                                    log.info(format(context, "Connected to Gateway"));
                                    emissionStrategy.emitNext(dispatch, GatewayStateChange.connected());
                                    observerState = GatewayObserver.CONNECTED;
                                } else {
                                    log.info(format(context, "Reconnected to Gateway"));
                                    emissionStrategy.emitNext(dispatch,
                                            GatewayStateChange.retrySucceeded(reconnectContext.getAttempts()));
                                    observerState = GatewayObserver.RETRY_SUCCEEDED;
                                }

                                reconnectContext.reset();
                                state.emitNext(GatewayConnection.State.CONNECTED, FAIL_FAST);
                                notifyObserver(observerState);
                            }))
                            .then();

                    // Subscribe the receiver to process and transform the inbound payloads into Dispatch events
                    Mono<Void> receiverFuture = receiver.asFlux()
                            .map(buf -> unpooled ? buf : buf.retain())
                            .doOnNext(buf -> logPayload(receiverLog, context, buf))
                            .flatMap(payloadReader::read)
                            .doOnDiscard(ByteBuf.class, DefaultGatewayClient::safeRelease)
                            .doOnNext(payload -> {
                                if (Opcode.HEARTBEAT_ACK.equals(payload.getOp())) {
                                    ping.emitEmpty(FAIL_FAST);
                                }
                            })
                            .map(this::updateSequence)
                            .flatMap(this::handlePayload)
                            .then();

                    // Subscribe the handler's outbound exchange with our outbound signals
                    // routing completion signals to close the gateway
                    Mono<Void> senderFuture = outbound.asFlux()
                            .doOnComplete(sessionHandler::close)
                            .doOnNext(payload -> {
                                if (Opcode.RECONNECT.equals(payload.getOp())) {
                                    sessionHandler.error(
                                            new GatewayException(context, "Reconnecting due to user action"));
                                }
                            })
                            .then();

                    // Create the heartbeat loop, and subscribe it using the sender sink
                    Mono<Void> heartbeatHandler = heartbeatEmitter.ticks()
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
                            .doOnNext(tick -> emissionStrategy.emitNext(heartbeats, tick))
                            .then();

                    Mono<Void> httpFuture = httpClient
                            .websocket(WebsocketClientSpec.builder()
                                    .maxFramePayloadLength(Integer.MAX_VALUE)
                                    .build())
                            .uri(gatewayUrl)
                            .handle(sessionHandler::handle)
                            .contextWrite(LogUtil.clearContext())
                            .flatMap(t2 -> handleClose(t2.getT1(), t2.getT2()))
                            .then();

                    return Mono.zip(httpFuture, readyHandler, receiverFuture, senderFuture, heartbeatHandler)
                            .doOnError(t -> {
                                if (t instanceof ReconnectException) {
                                    log.info(format(context, "{}"), t.getMessage());
                                } else if (t instanceof CloseException || t instanceof GatewayException) {
                                    log.warn(format(context, "{}"), t.toString());
                                } else {
                                    log.error(format(context, "Gateway client error"), t);
                                }
                            })
                            .doOnTerminate(heartbeatEmitter::stop)
                            .doOnCancel(() -> sessionHandler.close())
                            .then();
                })
                .contextWrite(ctx -> ctx.put(LogUtil.KEY_SHARD_ID, identifyOptions.getShardInfo().getIndex()))
                .retryWhen(retryFactory())
                .then(Mono.defer(() -> disconnectNotifier.asMono().then()))
                .doOnSubscribe(s -> {
                    if (disconnectNotifier != null) {
                        throw new IllegalStateException("execute can only be subscribed once");
                    }
                });
    }

    private HttpClient initHttpClient() {
        HttpClient client = reactorResources.getHttpClient()
                .headers(headers -> headers.add(USER_AGENT, initUserAgent()));
        if (observer == GatewayObserver.NOOP_LISTENER) {
            // don't apply an observer if the feature is not used
            return client;
        } else {
            return client.observe((connection, newState) -> notifyObserver(newState));
        }
    }

    private String initUserAgent() {
        final Properties properties = GitProperties.getProperties();
        final String version = properties.getProperty(GitProperties.APPLICATION_VERSION, "3");
        final String url = properties.getProperty(GitProperties.APPLICATION_URL, "https://discord4j.com");
        return "DiscordBot(" + url + ", " + version + ")";
    }

    private void notifyObserver(ConnectionObserver.State state) {
        observer.onStateChange(state, this);
    }

    private void logPayload(Logger logger, ContextView context, ByteBuf buf) {
        if (logger.isTraceEnabled()) {
            logger.trace(format(context, buf.toString(StandardCharsets.UTF_8)
                    .replaceAll("(\"token\": ?\")([A-Za-z0-9._-]*)(\")", "$1hunter2$3")));
        }
    }

    private static boolean isNotStartupPayload(GatewayPayload<?> payload) {
        return !Opcode.IDENTIFY.equals(payload.getOp()) && !Opcode.RESUME.equals(payload.getOp());
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

    @SuppressWarnings("unchecked")
    private <T extends PayloadData> Mono<Void> handlePayload(GatewayPayload<T> payload) {
        PayloadHandler<T> handler = (PayloadHandler<T>) handlerMap.get(payload.getOp());
        if (handler == null) {
            log.warn(format(currentContext, "Handler not found from: {}"), payload);
            return Mono.empty();
        }
        return Mono.defer(() -> handler.handle(payload))
                .checkpoint("Dispatch handled for OP " + payload.getOp().getRawOp() +
                        " seq " + payload.getSequence() + " type " + payload.getType());
    }

    private Mono<Void> handleDispatch(GatewayPayload<Dispatch> payload) {
        if (payload.getData() instanceof Ready) {
            String newSessionId = ((Ready) payload.getData()).sessionId();
            sessionId.set(newSessionId);
        }
        if (payload.getData() != null) {
            emissionStrategy.emitNext(dispatch, payload.getData());
        }
        return Mono.empty();
    }

    private Mono<Void> handleHeartbeat(GatewayPayload<Heartbeat> payload) {
        log.debug(format(currentContext, "Received heartbeat"));
        emissionStrategy.emitNext(outbound, GatewayPayload.heartbeat(ImmutableHeartbeat.of(sequence.get())));
        return Mono.empty();
    }

    private Mono<Void> handleReconnect(GatewayPayload<?> payload) {
        sessionHandler.error(new ReconnectException(currentContext, "Reconnecting due to reconnect packet received"));
        return Mono.empty();
    }

    private Mono<Void> handleInvalidSession(GatewayPayload<InvalidSession> payload) {
        //noinspection ConstantConditions
        if (payload.getData().resumable()) {
            emissionStrategy.emitNext(outbound,
                    GatewayPayload.resume(ImmutableResume.of(token, sessionId.get(), sequence.get())));
        } else {
            sessionHandler.error(new InvalidSessionException(currentContext,
                    "Reconnecting due to non-resumable session invalidation"));
        }
        return Mono.empty();
    }

    private Mono<Void> handleHello(GatewayPayload<Hello> payload) {
        //noinspection ConstantConditions
        Duration interval = Duration.ofMillis(payload.getData().heartbeatInterval());
        heartbeatEmitter.start(Duration.ZERO, interval);
        return state.asFlux()
                .next()
                .doOnNext(state -> {
                    if (state == GatewayConnection.State.START_RESUMING || state == GatewayConnection.State.RESUMING) {
                        doResume(payload);
                    } else {
                        doIdentify(payload);
                    }
                })
                .then();
    }

    private void doResume(GatewayPayload<Hello> payload) {
        log.debug(format(currentContext, "Resuming Gateway session from {}"), sequence.get());
        emissionStrategy.emitNext(outbound,
                GatewayPayload.resume(ImmutableResume.of(token, sessionId.get(), sequence.get())));
    }

    private void doIdentify(GatewayPayload<Hello> payload) {
        IdentifyProperties props = ImmutableIdentifyProperties.of(System.getProperty("os.name"), "Discord4J",
                "Discord4J");
        Identify identify = Identify.builder()
                .token(token)
                .intents(identifyOptions.getIntents().map(set -> Possible.of(set.getRawValue())).orElse(Possible.absent()))
                .properties(props)
                .compress(false)
                .largeThreshold(identifyOptions.getLargeThreshold())
                .shard(identifyOptions.getShardInfo().asArray())
                .presence(identifyOptions.getInitialStatus().map(Possible::of).orElse(Possible.absent()))
                .build();
        log.debug(format(currentContext, "Identifying to Gateway"), sequence.get());
        emissionStrategy.emitNext(outbound, GatewayPayload.identify(identify));
    }

    private Mono<Void> handleHeartbeatAck(GatewayPayload<?> context) {
        responseTime = lastAck.updateAndGet(x -> System.nanoTime()) - lastSent.get();
        missedAck.set(0);
        log.debug(format(currentContext, "Heartbeat acknowledged after {}"), getResponseTime());
        return Mono.empty();
    }

    private Retry retryFactory() {
        return GatewayRetrySpec.create(reconnectOptions, reconnectContext)
                .doBeforeRetry(retry -> {
                    state.emitNext(retry.nextState(), FAIL_FAST);
                    long attempt = retry.iteration();
                    Duration backoff = retry.nextBackoff();
                    log.debug(format(getContextFromException(retry.failure()),
                            "{} in {} (attempts: {})"), retry.nextState(), backoff, attempt);
                    if (retry.iteration() == 1) {
                        if (retry.nextState() == GatewayConnection.State.RESUMING) {
                            emissionStrategy.emitNext(dispatch, GatewayStateChange.retryStarted(backoff));
                            notifyObserver(GatewayObserver.RETRY_STARTED);
                        } else {
                            emissionStrategy.emitNext(dispatch, GatewayStateChange.retryStartedResume(backoff));
                            notifyObserver(GatewayObserver.RETRY_RESUME_STARTED);
                        }
                    } else {
                        emissionStrategy.emitNext(dispatch, GatewayStateChange.retryFailed(attempt - 1, backoff));
                        notifyObserver(GatewayObserver.RETRY_FAILED);
                    }
                    if (retry.nextState() == GatewayConnection.State.RECONNECTING) {
                        emissionStrategy.emitNext(dispatch, GatewayStateChange.sessionInvalidated());
                    }
                });
    }

    private ContextView getContextFromException(Throwable t) {
        if (t instanceof CloseException) {
            return ((CloseException) t).getContext();
        }
        if (t instanceof GatewayException) {
            return ((GatewayException) t).getContext();
        }
        return Context.empty();
    }

    private Mono<CloseStatus> handleClose(DisconnectBehavior sourceBehavior, CloseStatus closeStatus) {
        return Mono.deferContextual(ctx -> {
            DisconnectBehavior behavior;
            if (/*~~>*/GatewayRetrySpec.NON_RETRYABLE_STATUS_CODES.contains(closeStatus.getCode())) {
                // non-retryable close codes are non-transient errors therefore stopping is the only choice
                behavior = DisconnectBehavior.stop(sourceBehavior.getCause());
            } else {
                behavior = sourceBehavior;
            }
            log.debug(format(ctx, "Closing and {} with status {}"), behavior, closeStatus);
            state.emitNext(GatewayConnection.State.DISCONNECTING, FAIL_FAST);
            heartbeatEmitter.stop();

            if (behavior.getAction() == DisconnectBehavior.Action.STOP_ABRUPTLY) {
                emissionStrategy.emitNext(dispatch, GatewayStateChange.disconnectedResume());
                notifyObserver(GatewayObserver.DISCONNECTED_RESUME);
            } else if (behavior.getAction() == DisconnectBehavior.Action.STOP) {
                emissionStrategy.emitNext(dispatch, GatewayStateChange.disconnected(sourceBehavior, closeStatus));
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
                    state.emitNext(GatewayConnection.State.DISCONNECTED, FAIL_FAST);
                    if (behavior.getCause() != null) {
                        return Mono.just(new CloseException(closeStatus, ctx, behavior.getCause()))
                                .flatMap(ex -> {
                                    disconnectNotifier.emitError(ex, FAIL_FAST);
                                    return Mono.error(ex);
                                });
                    }
                    return Mono.just(closeStatus)
                            .doOnNext(status -> disconnectNotifier.emitValue(closeStatus, FAIL_FAST));
                case RETRY_ABRUPTLY:
                case RETRY:
                default:
                    return Mono.error(new CloseException(closeStatus, ctx, behavior.getCause()));
            }
        });
    }

    @Override
    public Mono<CloseStatus> close(boolean allowResume) {
        return Mono.defer(() -> {
            if (sessionHandler == null || disconnectNotifier == null) {
                return Mono.error(new IllegalStateException("Gateway client is not active!"));
            }
            if (allowResume) {
                sessionHandler.close(DisconnectBehavior.stopAbruptly(null));
            } else {
                sessionHandler.close(DisconnectBehavior.stop(null));
            }
            return disconnectNotifier.asMono();
        });
    }

    @Override
    public Flux<Dispatch> dispatch() {
        return dispatch.asFlux();
    }

    @Override
    public Flux<GatewayPayload<?>> receiver() {
        return receiver(payloadReader::read);
    }

    @Override
    public <T> Flux<T> receiver(Function<ByteBuf, Publisher<? extends T>> mapper) {
        return receiver.asFlux()
                .map(ByteBuf::retainedDuplicate)
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
    public Sinks.Many<GatewayPayload<?>> sender() {
        return outbound;
    }

    @Override
    public Mono<Void> sendBuffer(Publisher<ByteBuf> publisher) {
        return Flux.from(publisher).doOnNext(buf -> emissionStrategy.emitNext(sender, buf)).then();
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
        return state.asFlux();
    }

    @Override
    public Mono<Boolean> isConnected() {
        return state.asFlux().next()
                .filter(s -> s == GatewayConnection.State.CONNECTED)
                .hasElement()
                .defaultIfEmpty(false);
    }

    @Override
    public Duration getResponseTime() {
        return Duration.ofNanos(responseTime);
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
