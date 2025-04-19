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
import discord4j.common.LogUtil;
import discord4j.common.ResettableInterval;
import discord4j.common.close.CloseException;
import discord4j.common.close.CloseStatus;
import discord4j.common.close.DisconnectBehavior;
import discord4j.common.retry.ReconnectContext;
import discord4j.common.retry.ReconnectOptions;
import discord4j.common.sinks.EmissionStrategy;
import discord4j.common.util.Snowflake;
import discord4j.voice.crypto.EncryptionMode;
import discord4j.voice.json.*;
import discord4j.voice.retry.VoiceGatewayException;
import discord4j.voice.retry.VoiceGatewayReconnectException;
import discord4j.voice.retry.VoiceGatewayRetrySpec;
import discord4j.voice.retry.VoiceServerUpdateReconnectException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.*;
import reactor.function.TupleUtils;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;
import reactor.util.context.ContextView;
import reactor.util.retry.Retry;
import reactor.util.retry.RetrySpec;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static discord4j.common.LogUtil.format;
import static io.netty.handler.codec.http.HttpHeaderNames.USER_AGENT;
import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

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

    private final Snowflake guildId;
    private final Snowflake selfId;
    private final Function<VoiceGatewayPayload<?>, Mono<ByteBuf>> payloadWriter;
    private final Function<ByteBuf, Mono<? super VoiceGatewayPayload<?>>> payloadReader;
    private final VoiceReactorResources reactorResources;
    private final ReconnectOptions reconnectOptions;
    private final ReconnectContext reconnectContext;
    private final AudioProvider audioProvider;
    @SuppressWarnings("deprecation")
    private final AudioReceiver audioReceiver;
    private final VoiceSendTaskFactory sendTaskFactory;
    private final VoiceReceiveTaskFactory receiveTaskFactory;
    private final VoiceDisconnectTask disconnectTask;
    private final VoiceServerUpdateTask serverUpdateTask;
    private final VoiceChannelRetrieveTask channelRetrieveTask;
    private final Duration ipDiscoveryTimeout;
    private final RetrySpec ipDiscoveryRetrySpec;

    private final HttpClient httpClient;
    private final VoiceSocket voiceSocket;
    private final ResettableInterval heartbeat;
    private final Disposable.Swap cleanup;
    private final EmissionStrategy emissionStrategy;

    /**
     * Payloads coming from the websocket.
     */
    private final Sinks.Many<ByteBuf> receiver;

    /**
     * Outbound voice gateway events.
     */
    private final Sinks.Many<VoiceGatewayPayload<?>> outbound;

    /**
     * Inbound voice gateway events from {@code receiver} that are pushed to consumers.
     */
    private final Sinks.Many<VoiceGatewayEvent> events;

    /**
     * Internal connection state changes are reflected as emissions to this sink.
     */
    private final Sinks.Many<VoiceConnection.State> state;

    private final AtomicReference<VoiceServerOptions> serverOptions = new AtomicReference<>();
    private final AtomicReference<String> session = new AtomicReference<>();

    private volatile int ssrc;
    private volatile Sinks.One<CloseStatus> disconnectNotifier;
    private volatile ContextView currentContext;
    private volatile VoiceWebsocketHandler sessionHandler;
    private EncryptionMode encryptionMode;

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
        this.channelRetrieveTask = Objects.requireNonNull(options.getChannelRetrieveTask());
        this.ipDiscoveryTimeout = Objects.requireNonNull(options.getIpDiscoveryTimeout());
        this.ipDiscoveryRetrySpec = Objects.requireNonNull(options.getIpDiscoveryRetrySpec());

        this.httpClient = reactorResources.getHttpClient()
                .headers(headers -> headers.add(USER_AGENT, "DiscordBot(https://discord4j.com, 3)"));
        this.voiceSocket = new VoiceSocket(reactorResources.getUdpClient());
        this.heartbeat = new ResettableInterval(reactorResources.getTimerTaskScheduler());
        this.cleanup = Disposables.swap();
        this.emissionStrategy = EmissionStrategy.timeoutDrop(Duration.ofSeconds(5));

        this.receiver = newEmitterSink();
        this.outbound = newEmitterSink();
        this.events = newEmitterSink();
        this.state = Sinks.many().replay().latestOrDefault(VoiceConnection.State.CONNECTING);
    }

    private static <T> Sinks.Many<T> newEmitterSink() {
        return Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
    }

    public Mono<VoiceConnection> start(VoiceServerOptions voiceServerOptions, String session) {
        return Mono.create(sink -> sink.onRequest(d -> {

            Disposable.Composite outerCleanup = Disposables.composite();

            outerCleanup.add(serverUpdateTask.onVoiceServerUpdates(guildId)
                    .subscribe(newValue -> {
                        VoiceServerOptions current = serverOptions.get();
                        if (!current.getEndpoint().equals(newValue.getEndpoint())) {
                            log.debug(format(sink.currentContext(), "Voice server endpoint change: {}"),
                                    current.getEndpoint(), newValue.getEndpoint());
                            serverOptions.set(newValue);
                            if (sessionHandler != null) {
                                sessionHandler.close(DisconnectBehavior.retryAbruptly(
                                        new VoiceServerUpdateReconnectException(sink.currentContext())));
                            }
                        }
                    }));

            outerCleanup.add(connect(voiceServerOptions, session, sink)
                    .contextWrite(sink.currentContext())
                    .subscribe(null,
                            t -> log.debug(format(sink.currentContext(), "Voice gateway error: {}"), t.toString()),
                            () -> log.debug(format(sink.currentContext(), "Voice gateway completed"))
                    ));

            sink.onCancel(outerCleanup);
        }));
    }

    private Mono<Void> connect(VoiceServerOptions vso, String sessionId,
                               MonoSink<VoiceConnection> voiceConnectionSink) {
        return Mono.deferContextual(
                        context -> {
                            serverOptions.compareAndSet(null, vso);
                            session.compareAndSet(null, sessionId);
                            disconnectNotifier = Sinks.one();
                            currentContext = context;

                            Flux<ByteBuf> outFlux = outbound.asFlux()
                                    .flatMap(payloadWriter)
                                    .doOnNext(buf -> logPayload(senderLog, context, buf));

                            sessionHandler = new VoiceWebsocketHandler(receiver, outFlux, context);

                            Mono<?> onOpen = state.asFlux()
                                    .next()
                                    .doOnNext(s -> {
                                        if (s == VoiceConnection.State.RESUMING) {
                                            log.info(format(context, "Attempting to resume"));
                                            emissionStrategy.emitNext(outbound, new Resume(guildId.asString(),
                                                    session.get(),
                                                    serverOptions.get().getToken()));
                                        } else {
                                            nextState(VoiceConnection.State.CONNECTING);
                                            log.info(format(context, "Identifying"));
                                            emissionStrategy.emitNext(outbound, new Identify(guildId.asString(),
                                                    selfId.asString(), session.get(), serverOptions.get().getToken()));
                                        }
                                    });

                            Disposable.Composite innerCleanup = Disposables.composite();

                            Mono<Void> receiverFuture = receiver.asFlux()
                                    .doOnNext(buf -> logPayload(receiverLog, context, buf))
                                    .flatMap(payloadReader)
                                    .doOnNext(payload -> {
                                        if (payload instanceof Hello) {
                                            Hello hello = (Hello) payload;
                                            Duration interval =
                                                    Duration.ofMillis(hello.getData().getHeartbeatInterval());
                                            heartbeat.start(interval, interval);
                                        } else if (payload instanceof Ready) {
                                            log.info(format(context, "Waiting for session description"));
                                            Ready ready = (Ready) payload;
                                            ssrc = ready.getData().getSsrc();
                                            cleanup.update(innerCleanup);
                                            innerCleanup.add(Mono.defer(() ->
                                                            voiceSocket.setup(ready.getData().getIp(),
                                                                    ready.getData().getPort()))
                                                    .zipWith(voiceSocket.performIpDiscovery(ready.getData().getSsrc()))
                                                    .timeout(ipDiscoveryTimeout)
                                                    .retryWhen(ipDiscoveryRetrySpec)
                                                    .contextWrite(context)
                                                    .onErrorMap(t -> new VoiceGatewayException(context,
                                                            "UDP socket setup error", t))
                                                    .subscribe(TupleUtils.consumer((connection, address) -> {
                                                                innerCleanup.add(connection);
                                                                String hostName = address.getHostName();
                                                                int port = address.getPort();

                                                                this.encryptionMode = EncryptionMode.getBestMode();
                                                                if (this.encryptionMode == null) {
                                                                    nextState(VoiceConnection.State.DISCONNECTED);
                                                                    voiceConnectionSink.error(new IllegalStateException("No encryption mode available"));
                                                                    return;
                                                                }

                                                                log.info("Using encryption mode {}", this.encryptionMode.name());

                                                                emissionStrategy.emitNext(outbound,
                                                                        new SelectProtocol(VoiceSocket.PROTOCOL,
                                                                                hostName,
                                                                                port, encryptionMode.getValue()));
                                                            }),
                                                            t -> {
                                                                voiceConnectionSink.error(t);
                                                                sessionHandler.close(DisconnectBehavior.stop(t));
                                                            },
                                                            () -> log.debug(format(context, "Voice socket setup " +
                                                                    "complete"))));
                                        } else if (payload instanceof SessionDescription) {
                                            log.info(format(context, "Receiving events"));
                                            nextState(VoiceConnection.State.CONNECTED);
                                            reconnectContext.reset();
                                            SessionDescription sessionDescription = (SessionDescription) payload;
                                            byte[] secretKey = sessionDescription.getData().getSecretKey();

                                            PacketTransformer transformer;
                                            try {
                                                transformer = new PacketTransformer(ssrc, encryptionMode, secretKey);
                                            } catch (GeneralSecurityException e) {
                                                log.error("Failed to create packet transformer", e);
                                                nextState(VoiceConnection.State.DISCONNECTED);
                                                voiceConnectionSink.error(e);
                                                return;
                                            }

                                            Consumer<Boolean> speakingSender = speaking -> emissionStrategy.emitNext(
                                                    outbound, new SentSpeaking(speaking, 0, ssrc));
                                            innerCleanup.add(() -> log.debug(format(context, "Disposing voice tasks")));
                                            innerCleanup.add(sendTaskFactory.create(reactorResources.getSendTaskScheduler(),
                                                    speakingSender, voiceSocket::send, audioProvider, transformer));
                                            innerCleanup.add(receiveTaskFactory.create(reactorResources.getReceiveTaskScheduler(),
                                                    voiceSocket.getInbound(), transformer, audioReceiver));
                                            voiceConnectionSink.success(acquireConnection());
                                        } else if (payload instanceof Resumed) {
                                            log.info(format(context, "Resumed"));
                                            nextState(VoiceConnection.State.CONNECTED);
                                            reconnectContext.reset();
                                        }

                                        emissionStrategy.emitNext(events, (VoiceGatewayEvent) payload);
                                    })
                                    .then();

                            Mono<Void> heartbeatHandler = heartbeat.ticks()
                                    .map(Heartbeat::new)
                                    .doOnNext(tick -> emissionStrategy.emitNext(outbound, tick))
                                    .then();

                            String fullEndpoint = serverOptions.get().getEndpoint();
                            log.debug("Using endpoint {}", fullEndpoint);

                            Mono<Void> httpFuture = httpClient
                                    .websocket(WebsocketClientSpec.builder()
                                            .maxFramePayloadLength(Integer.MAX_VALUE)
                                            .build())
                                    .uri(fullEndpoint)
                                    .handle((in, out) -> onOpen.then(sessionHandler.handle(in, out)))
                                    .contextWrite(LogUtil.clearContext())
                                    .flatMap(t2 -> handleClose(t2.getT1(), t2.getT2()))
                                    .then();

                            return Mono.zip(httpFuture, receiverFuture, heartbeatHandler)
                                    .doOnError(t -> log.error(format(context, "{}"), t.toString()))
                                    .doOnTerminate(heartbeat::stop)
                                    .doOnCancel(() -> sessionHandler.close())
                                    .then();
                        })
                .contextWrite(ctx -> ctx.put(LogUtil.KEY_GUILD_ID, guildId.asString()))
                .retryWhen(retryFactory())
                .then(Mono.defer(() -> disconnectNotifier.asMono().then()))
                .doOnSubscribe(s -> {
                    if (disconnectNotifier != null) {
                        throw new IllegalStateException("connect can only be subscribed once");
                    }
                });
    }

    private void nextState(VoiceConnection.State value) {
        log.debug(format(currentContext, "New state: {}"), value);
        emissionStrategy.emitNext(state, value);
    }

    private VoiceConnection acquireConnection() {
        // TODO improve VoiceConnection API
        return new VoiceConnection() {

            @Override
            public Flux<VoiceGatewayEvent> events() {
                return events.asFlux();
            }

            @Override
            public Flux<State> stateEvents() {
                return state.asFlux();
            }

            @Override
            public Mono<Void> disconnect() {
                return onConnectOrDisconnect()
                        .flatMap(s -> s.equals(State.CONNECTED) ? stop() : Mono.empty())
                        .then();
            }

            @Override
            public Snowflake getGuildId() {
                return guildId;
            }

            @Override
            public Mono<Snowflake> getChannelId() {
                return onConnectOrDisconnect()
                        .flatMap(s -> s.equals(State.CONNECTED) ? channelRetrieveTask.onRequest() : Mono.empty());
            }

            @Override
            public Mono<Void> reconnect() {
                return reconnect(VoiceGatewayReconnectException::new);
            }

            @Override
            public Mono<Void> reconnect(Function<ContextView, Throwable> errorCause) {
                return onConnectOrDisconnect()
                        .flatMap(s -> s.equals(State.CONNECTED) ?
                                Mono.fromRunnable(() -> sessionHandler.close(
                                                DisconnectBehavior.retryAbruptly(
                                                        errorCause.apply(currentContext))))
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
            return disconnectNotifier.asMono().then();
        });
    }

    private void logPayload(Logger logger, ContextView context, ByteBuf buf) {
        if (logger.isTraceEnabled()) {
            logger.trace(format(context, buf.toString(StandardCharsets.UTF_8)
                    .replaceAll("(\"token\": ?\")([A-Za-z0-9._-]*)(\")", "$1hunter2$3")));
        }
    }

    private Retry retryFactory() {
        return VoiceGatewayRetrySpec.create(reconnectOptions, reconnectContext)
                .doBeforeRetry(retry -> {
                    nextState(retry.nextState());
                    long attempt = retry.iteration();
                    Duration backoff = retry.nextBackoff();
                    log.debug(format(getContextFromException(retry.failure()),
                            "{} in {} (attempts: {})"), retry.nextState(), backoff, attempt);
                });
    }

    private ContextView getContextFromException(Throwable t) {
        if (t instanceof CloseException) {
            return ((CloseException) t).getContext();
        }
        if (t instanceof VoiceGatewayException) {
            return ((VoiceGatewayException) t).getContext();
        }
        return Context.empty();
    }

    private Mono<CloseStatus> handleClose(DisconnectBehavior sourceBehavior, CloseStatus closeStatus) {
        return Mono.deferContextual(ctx -> {
            DisconnectBehavior behavior;
            if (VoiceGatewayRetrySpec.NON_RETRYABLE_STATUS_CODES.contains(closeStatus.getCode())) {
                // non-retryable close codes are non-transient errors therefore stopping is the only choice
                behavior = DisconnectBehavior.stop(sourceBehavior.getCause());
            } else {
                behavior = sourceBehavior;
            }
            log.debug(format(ctx, "Closing and {} with status {}"), behavior, closeStatus);
            heartbeat.stop();

            if (behavior.getAction() == DisconnectBehavior.Action.STOP) {
                cleanup.dispose();
            }

            switch (behavior.getAction()) {
                case STOP_ABRUPTLY:
                case STOP:
                    if (behavior.getCause() != null) {
                        return Mono.just(new CloseException(closeStatus, ctx, behavior.getCause()))
                                .flatMap(ex -> {
                                    nextState(VoiceConnection.State.DISCONNECTED);
                                    disconnectNotifier.emitError(ex, FAIL_FAST);
                                    Mono<CloseStatus> thenMono = closeStatus.getCode() == 4014 ?
                                            Mono.just(closeStatus) : Mono.error(ex);
                                    return disconnectTask.onDisconnect(guildId).then(thenMono);
                                });
                    }
                    return Mono.just(closeStatus)
                            .flatMap(status -> {
                                nextState(VoiceConnection.State.DISCONNECTED);
                                disconnectNotifier.emitValue(closeStatus, FAIL_FAST);
                                return disconnectTask.onDisconnect(guildId).thenReturn(closeStatus);
                            });
                case RETRY_ABRUPTLY:
                case RETRY:
                default:
                    // reconnect should be handled now by retryFactory
                    return Mono.error(new CloseException(closeStatus, ctx, behavior.getCause()));
            }
        });
    }

}
