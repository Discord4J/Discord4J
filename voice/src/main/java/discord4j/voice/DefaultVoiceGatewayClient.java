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

package discord4j.voice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwebpp.crypto.TweetNaclFast;
import discord4j.common.LogUtil;
import discord4j.common.ResettableInterval;
import discord4j.common.close.CloseException;
import discord4j.common.close.CloseStatus;
import discord4j.common.close.DisconnectBehavior;
import discord4j.common.retry.ReconnectContext;
import discord4j.common.retry.ReconnectOptions;
import discord4j.voice.json.*;
import discord4j.voice.retry.PartialDisconnectException;
import discord4j.voice.retry.VoiceGatewayException;
import discord4j.voice.retry.VoiceGatewayReconnectException;
import discord4j.voice.retry.VoiceServerUpdateReconnectException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.*;
import reactor.function.TupleUtils;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.WebsocketClientSpec;
import reactor.retry.Retry;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;
import reactor.util.retry.RetrySpec;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static discord4j.common.LogUtil.format;
import static io.netty.handler.codec.http.HttpHeaderNames.USER_AGENT;

/**
 * A default implementation for client that is able to connect to Discord Voice Gateway and establish a
 * {@link VoiceConnection} capable of sending and receiving audio.
 *
 * @see <a href="https://discord.com/developers/docs/topics/voice-connections">Voice</a>
 */
public class DefaultVoiceGatewayClient {

    private static final Logger log = Loggers.getLogger(DefaultVoiceGatewayClient.class);
    private static final Logger senderLog = Loggers.getLogger("discord4j.voice.protocol.sender");
    private static final Logger receiverLog = Loggers.getLogger("discord4j.voice.protocol.receiver");

    // reactive pipelines
    private final EmitterProcessor<ByteBuf> receiver = EmitterProcessor.create(false);
    private final EmitterProcessor<VoiceGatewayPayload<?>> outbound = EmitterProcessor.create(false);
    private final EmitterProcessor<VoiceGatewayEvent> events = EmitterProcessor.create(false);
    private final FluxSink<ByteBuf> receiverSink;
    private final FluxSink<VoiceGatewayPayload<?>> outboundSink;
    private final FluxSink<VoiceGatewayEvent> eventSink;

    private final long guildId;
    private final long selfId;
    private final Function<VoiceGatewayPayload<?>, Mono<ByteBuf>> payloadWriter;
    private final Function<ByteBuf, Mono<? super VoiceGatewayPayload<?>>> payloadReader;
    private final VoiceReactorResources reactorResources;
    private final ReconnectOptions reconnectOptions;
    private final ReconnectContext reconnectContext;
    private final AudioProvider audioProvider;
    private final AudioReceiver audioReceiver;
    private final VoiceSendTaskFactory sendTaskFactory;
    private final VoiceReceiveTaskFactory receiveTaskFactory;
    private final VoiceDisconnectTask disconnectTask;
    private final VoiceServerUpdateTask serverUpdateTask;
    private final VoiceStateUpdateTask stateUpdateTask;
    private final VoiceChannelRetrieveTask channelRetrieveTask;
    private final Duration ipDiscoveryTimeout;
    private final RetrySpec ipDiscoveryRetrySpec;

    private final VoiceSocket voiceSocket;
    private final ResettableInterval heartbeat;
    private final Disposable.Swap cleanup = Disposables.swap();

    private final ReplayProcessor<VoiceConnection.State> state;
    private final FluxSink<VoiceConnection.State> stateChanges;

    private final AtomicBoolean allowResume = new AtomicBoolean(false);
    private final AtomicReference<VoiceServerOptions> serverOptions = new AtomicReference<>();
    private final AtomicReference<String> session = new AtomicReference<>();

    private volatile int ssrc;
    private volatile MonoProcessor<Void> disconnectNotifier;
    private volatile Context currentContext;
    private volatile VoiceWebsocketHandler sessionHandler;

    public DefaultVoiceGatewayClient(VoiceGatewayOptions options) {
        this.guildId = options.getGuildId();
        this.selfId = options.getSelfId();
        ObjectMapper mapper = Objects.requireNonNull(options.getJacksonResources()).getObjectMapper();
        // TODO improve allocation
        this.payloadWriter = payload ->
                Mono.fromCallable(() -> Unpooled.wrappedBuffer(mapper.writeValueAsBytes(payload)));
        this.payloadReader = buf -> Mono.fromCallable(() -> {
            @SuppressWarnings("UnnecessaryLocalVariable")
            VoiceGatewayPayload<?> payload = mapper.readValue(new ByteBufInputStream(buf),
                    new TypeReference<VoiceGatewayPayload<?>>() {});
            return payload;
        });
        this.reactorResources = Objects.requireNonNull(options.getReactorResources());
        this.reconnectOptions = Objects.requireNonNull(options.getReconnectOptions());
        this.reconnectContext = new ReconnectContext(reconnectOptions.getFirstBackoff(),
                reconnectOptions.getMaxBackoffInterval());
        this.audioProvider = Objects.requireNonNull(options.getAudioProvider());
        this.audioReceiver = Objects.requireNonNull(options.getAudioReceiver());
        this.sendTaskFactory = Objects.requireNonNull(options.getSendTaskFactory());
        this.receiveTaskFactory = Objects.requireNonNull(options.getReceiveTaskFactory());
        this.disconnectTask = Objects.requireNonNull(options.getDisconnectTask());
        this.serverUpdateTask = Objects.requireNonNull(options.getServerUpdateTask());
        this.stateUpdateTask = Objects.requireNonNull(options.getStateUpdateTask());
        this.channelRetrieveTask = Objects.requireNonNull(options.getChannelRetrieveTask());
        this.ipDiscoveryTimeout = Objects.requireNonNull(options.getIpDiscoveryTimeout());
        this.ipDiscoveryRetrySpec = Objects.requireNonNull(options.getIpDiscoveryRetrySpec());

        this.voiceSocket = new VoiceSocket(reactorResources.getUdpClient());
        this.heartbeat = new ResettableInterval(reactorResources.getTimerTaskScheduler());

        this.state = ReplayProcessor.cacheLastOrDefault(VoiceConnection.State.CONNECTING);
        this.stateChanges = state.sink(FluxSink.OverflowStrategy.LATEST);

        this.receiverSink = receiver.sink(FluxSink.OverflowStrategy.BUFFER);
        this.outboundSink = outbound.sink(FluxSink.OverflowStrategy.ERROR);
        this.eventSink = events.sink(FluxSink.OverflowStrategy.LATEST);
    }

    public Mono<VoiceConnection> start(VoiceServerOptions voiceServerOptions, String session) {
        return Mono.create(sink -> sink.onRequest(d -> {
            Disposable connect = connect(voiceServerOptions, session, sink)
                    .subscriberContext(sink.currentContext())
                    .subscribe(null,
                            t -> log.error(format(sink.currentContext(), "Voice gateway terminated with an error"), t),
                            () -> log.debug(format(sink.currentContext(), "Voice gateway completed")));
            sink.onCancel(connect);
        }));
    }

    private Mono<Void> connect(VoiceServerOptions vso, String sessionId,
                               MonoSink<VoiceConnection> voiceConnectionSink) {
        return Mono.deferWithContext(
                context -> {
                    serverOptions.compareAndSet(null, vso);
                    session.compareAndSet(null, sessionId);
                    disconnectNotifier = MonoProcessor.create();
                    currentContext = context;

                    Flux<ByteBuf> outFlux = outbound.flatMap(payloadWriter)
                            .doOnNext(buf -> logPayload(senderLog, context, buf));

                    sessionHandler = new VoiceWebsocketHandler(receiverSink, outFlux, context);

                    // TODO: validate this resume flow
                    if (allowResume.get()) {
                        stateChanges.next(VoiceConnection.State.CONNECTING);
                        log.info(format(context, "Attempting to resume"));
                        outboundSink.next(new Resume(Long.toUnsignedString(guildId),
                                Long.toUnsignedString(selfId), session.get()));
                    }

                    Disposable.Composite innerCleanup = Disposables.composite();

                    Mono<Void> receiverFuture = receiver.doOnNext(buf -> logPayload(receiverLog, context, buf))
                            .flatMap(payloadReader)
                            .doOnNext(payload -> {
                                if (!allowResume.get() && payload instanceof Hello) {
                                    stateChanges.next(VoiceConnection.State.CONNECTING);
                                    Hello hello = (Hello) payload;
                                    Duration interval = Duration.ofMillis(hello.getData().heartbeatInterval);
                                    heartbeat.start(interval, interval);
                                    log.info(format(context, "Identifying"));
                                    outboundSink.next(new Identify(Long.toUnsignedString(guildId),
                                            Long.toUnsignedString(selfId), session.get(),
                                            serverOptions.get().getToken()));
                                } else if (payload instanceof Ready) {
                                    log.info(format(context, "Waiting for session description"));
                                    Ready ready = (Ready) payload;
                                    ssrc = ready.getData().ssrc;
                                    cleanup.update(innerCleanup);
                                    innerCleanup.add(Mono.defer(() ->
                                            voiceSocket.setup(ready.getData().ip, ready.getData().port))
                                            .zipWith(voiceSocket.performIpDiscovery(ready.getData().ssrc))
                                            .timeout(ipDiscoveryTimeout)
                                            .retryWhen(ipDiscoveryRetrySpec)
                                            .subscriberContext(context)
                                            .onErrorMap(t -> new VoiceGatewayException(context,
                                                    "UDP socket setup error", t))
                                            .subscribe(TupleUtils.consumer((connection, address) -> {
                                                        innerCleanup.add(connection);
                                                        String hostName = address.getHostName();
                                                        int port = address.getPort();
                                                        outboundSink.next(new SelectProtocol(VoiceSocket.PROTOCOL,
                                                                hostName,
                                                                port, VoiceSocket.ENCRYPTION_MODE));
                                                    }),
                                                    t -> {
                                                        voiceConnectionSink.error(t);
                                                        sessionHandler.close(DisconnectBehavior.stop(t));
                                                    },
                                                    () -> log.debug(format(context, "Voice socket setup complete"))));
                                } else if (payload instanceof SessionDescription) {
                                    log.info(format(context, "Receiving events"));
                                    stateChanges.next(VoiceConnection.State.CONNECTED);
                                    allowResume.set(true);
                                    reconnectContext.reset();
                                    SessionDescription sessionDescription = (SessionDescription) payload;
                                    byte[] secretKey = sessionDescription.getData().secretKey;
                                    TweetNaclFast.SecretBox boxer = new TweetNaclFast.SecretBox(secretKey);
                                    PacketTransformer transformer = new PacketTransformer(ssrc, boxer);
                                    Consumer<Boolean> speakingSender = speaking ->
                                            outboundSink.next(new SentSpeaking(speaking, 0, ssrc));
                                    innerCleanup.add(() -> log.info(format(context, "Disposing voice tasks")));
                                    innerCleanup.add(sendTaskFactory.create(reactorResources.getSendTaskScheduler(),
                                            speakingSender, voiceSocket::send, audioProvider, transformer));
                                    innerCleanup.add(receiveTaskFactory.create(reactorResources.getReceiveTaskScheduler(),
                                            voiceSocket.getInbound(), transformer, audioReceiver));
                                    innerCleanup.add(serverUpdateTask.onVoiceServerUpdate(guildId)
                                            .subscribe(newValue -> {
                                                VoiceServerOptions current = serverOptions.get();
                                                if (!current.getEndpoint().equals(newValue.getEndpoint())) {
                                                    log.info(format(context, "Voice server endpoint change: {}"),
                                                            current.getEndpoint(), newValue.getEndpoint());
                                                    serverOptions.set(newValue);
                                                    sessionHandler.close(DisconnectBehavior.retryAbruptly(
                                                            new VoiceServerUpdateReconnectException(context)));
                                                }
                                            }));
                                    // TODO consider for removal if we shouldn't do anything on these
                                    innerCleanup.add(stateUpdateTask.onVoiceStateUpdate(guildId)
                                            .subscribe(newValue -> {
                                                String current = session.get();
                                                if (!newValue.equals(current)) {
                                                    log.info(format(context, "Voice session updated"));
                                                    session.set(newValue);
                                                    // TODO if disposing the session turn this into a Mono
                                                }
                                            }));
                                    voiceConnectionSink.success(acquireConnection());
                                } else if (payload instanceof Resumed) {
                                    log.info(format(context, "Resumed"));
                                    stateChanges.next(VoiceConnection.State.CONNECTED);
                                    allowResume.set(true);
                                    reconnectContext.reset();
                                }
                                eventSink.next((VoiceGatewayEvent) payload);
                            })
                            .then();

                    Mono<Void> heartbeatHandler = heartbeat.ticks()
                            .map(Heartbeat::new)
                            .doOnNext(outboundSink::next)
                            .then();

                    Mono<Void> httpFuture = reactorResources.getHttpClient()
                            .headers(headers -> headers.add(USER_AGENT, "DiscordBot(https://discord4j.com, 3)"))
                            .observe(getObserver(context))
                            .websocket(WebsocketClientSpec.builder()
                                    .maxFramePayloadLength(Integer.MAX_VALUE)
                                    .build())
                            .uri(serverOptions.get().getEndpoint() + "?v=4")
                            .handle(sessionHandler::handle)
                            .subscriberContext(LogUtil.clearContext())
                            .flatMap(t2 -> handleClose(t2.getT1(), t2.getT2()))
                            .then();

                    return Mono.zip(httpFuture, receiverFuture, heartbeatHandler)
                            .doOnError(t -> log.error(format(context, "{}"), t.toString()))
                            .doOnTerminate(heartbeat::stop)
                            .doOnCancel(() -> sessionHandler.close())
                            .then();
                })
                .retryWhen(retryFactory())
                .then(Mono.defer(() -> disconnectNotifier));
    }

    private ConnectionObserver getObserver(Context context) {
        return (connection, newState) -> log.debug(format(context, "{} {}"), newState, connection);
    }

    private VoiceConnection acquireConnection() {
        // TODO improve VoiceConnection API
        return new VoiceConnection() {

            @Override
            public Flux<VoiceGatewayEvent> events() {
                return events;
            }

            @Override
            public Flux<State> stateEvents() {
                return state;
            }

            @Override
            public Mono<Void> disconnect() {
                return onConnectOrDisconnect()
                        .flatMap(s -> s.equals(State.CONNECTED) ? stop() : Mono.empty())
                        .then();
            }

            @Override
            public long getGuildId() {
                return guildId;
            }

            @Override
            public Mono<Long> getChannelId() {
                return onConnectOrDisconnect()
                        .flatMap(s -> s.equals(State.CONNECTED) ? channelRetrieveTask.onRequest() : Mono.empty());
            }

            @Override
            public Mono<Void> reconnect() {
                return onConnectOrDisconnect()
                        .flatMap(s -> s.equals(State.CONNECTED) ?
                                Mono.fromRunnable(() -> sessionHandler.close(
                                        DisconnectBehavior.retryAbruptly(
                                                new VoiceGatewayReconnectException(currentContext))))
                                        .then(stateEvents()
                                                .filter(ss -> ss.equals(State.CONNECTED))
                                                .next()) :
                                Mono.error(new IllegalStateException("Voice connection has already disconnected")))
                        .then();
            }
        };
    }

    public Mono<Void> stop() {
        return Mono.defer(() -> {
            if (sessionHandler == null || disconnectNotifier == null) {
                return Mono.error(new IllegalStateException("Gateway client is not active!"));
            }
            sessionHandler.close(DisconnectBehavior.stop(null));
            return disconnectNotifier;
        });
    }

    private void logPayload(Logger logger, Context context, ByteBuf buf) {
        logger.trace(format(context, buf.toString(StandardCharsets.UTF_8)
                .replaceAll("(\"token\": ?\")([A-Za-z0-9._-]*)(\")", "$1hunter2$3")));
    }

    private Retry<ReconnectContext> retryFactory() {
        return Retry.<ReconnectContext>onlyIf(t -> isRetriable(t.exception()))
                .withApplicationContext(reconnectContext)
                .withBackoffScheduler(reconnectOptions.getBackoffScheduler())
                .backoff(reconnectOptions.getBackoff())
                .jitter(reconnectOptions.getJitter())
                .retryMax(reconnectOptions.getMaxRetries())
                .doOnRetry(retryContext -> {
                    stateChanges.next(VoiceConnection.State.RECONNECTING);
                    int attempt = retryContext.applicationContext().getAttempts();
                    Duration backoff = retryContext.backoff();
                    log.info(format(getContextFromException(retryContext.exception()),
                            "Reconnect attempt {} in {}"), attempt, backoff);
                    if (attempt == 1) {
                        if (!allowResume.get() || !canResume(retryContext.exception())) {
                            allowResume.set(false);
                        } else {
                            log.info(format(getContextFromException(retryContext.exception()), "Resume is available"));
                        }
                    } else {
                        allowResume.set(false);
                    }
                    retryContext.applicationContext().next();
                });
    }

    private static final List<Integer> nonRetriableStatusCodes = Arrays.asList(
            4004, // Authentication failed
            4006, // Session no longer valid
            4014, // Disconnected
            4016 // Unknown encryption mode
    );

    private boolean isRetriable(@Nullable Throwable t) {
        if (t instanceof CloseException) {
            CloseException closeException = (CloseException) t;
            return !nonRetriableStatusCodes.contains(closeException.getCode());
        }
        return !(t instanceof PartialDisconnectException);
    }

    private boolean canResume(Throwable t) {
        if (t instanceof CloseException) {
            CloseException closeException = (CloseException) t;
            return closeException.getCode() < 4000;
        }
        return !(t instanceof VoiceGatewayReconnectException);
    }

    private Context getContextFromException(Throwable t) {
        if (t instanceof CloseException) {
            return ((CloseException) t).getContext();
        }
        if (t instanceof VoiceGatewayException) {
            return ((VoiceGatewayException) t).getContext();
        }
        return Context.empty();
    }

    private Mono<CloseStatus> handleClose(DisconnectBehavior sourceBehavior, CloseStatus closeStatus) {
        return Mono.deferWithContext(ctx -> {
            DisconnectBehavior behavior;
            if (nonRetriableStatusCodes.contains(closeStatus.getCode())) {
                // non-retryable close codes are non-transient errors therefore stopping is the only choice
                behavior = DisconnectBehavior.stop(sourceBehavior.getCause());
            } else {
                behavior = sourceBehavior;
            }
            log.info(format(ctx, "Handling close {} with behavior: {}"), closeStatus, behavior);
            heartbeat.stop();

            if (behavior.getAction() == DisconnectBehavior.Action.STOP) {
                allowResume.set(false);
            }

            if (!allowResume.get()) {
                cleanup.dispose();
            }

            switch (behavior.getAction()) {
                case STOP_ABRUPTLY:
                case STOP:
                    stateChanges.next(VoiceConnection.State.DISCONNECTED);
                    disconnectNotifier.onComplete();
                    return disconnectTask.onDisconnect(guildId).thenReturn(closeStatus);
                case RETRY_ABRUPTLY:
                case RETRY:
                default:
                    // reconnect should be handled now by retryFactory
                    return Mono.error(new CloseException(closeStatus, ctx, behavior.getCause()));
            }
        });
    }

}
