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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.*;
import reactor.retry.Retry;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.context.Context;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static discord4j.common.LogUtil.format;
import static io.netty.handler.codec.http.HttpHeaderNames.USER_AGENT;

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
    private final String sessionId;
    private final String token;
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
    private final VoiceSocket voiceSocket;
    private final ResettableInterval heartbeat;

    private final AtomicReference<VoiceConnection.State> state =
            new AtomicReference<>(VoiceConnection.State.DISCONNECTED);
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean allowResume = new AtomicBoolean(false);
    private volatile int ssrc;
    private volatile MonoProcessor<Void> disconnectNotifier;
    private volatile VoiceWebsocketHandler sessionHandler;
    private final Disposable.Swap cleanup = Disposables.swap();

    public DefaultVoiceGatewayClient(long guildId, long selfId, String sessionId, String token, ObjectMapper mapper,
                                     VoiceReactorResources reactorResources, ReconnectOptions reconnectOptions,
                                     AudioProvider audioProvider, AudioReceiver audioReceiver,
                                     VoiceSendTaskFactory sendTaskFactory, VoiceReceiveTaskFactory receiveTaskFactory,
                                     VoiceDisconnectTask disconnectTask) {
        this.guildId = guildId;
        this.selfId = selfId;
        this.sessionId = Objects.requireNonNull(sessionId);
        this.token = Objects.requireNonNull(token);
        this.payloadWriter = payload ->
                Mono.fromCallable(() -> Unpooled.wrappedBuffer(mapper.writeValueAsBytes(payload)));
        this.payloadReader = buf -> Mono.fromCallable(() -> {
            @SuppressWarnings("UnnecessaryLocalVariable")
            VoiceGatewayPayload<?> payload = mapper.readValue(new ByteBufInputStream(buf),
                    new TypeReference<VoiceGatewayPayload<?>>() {});
            return payload;
        });
        this.reactorResources = Objects.requireNonNull(reactorResources);
        this.reconnectOptions = Objects.requireNonNull(reconnectOptions);
        this.reconnectContext = new ReconnectContext(reconnectOptions.getFirstBackoff(),
                reconnectOptions.getMaxBackoffInterval());
        this.audioProvider = Objects.requireNonNull(audioProvider);
        this.audioReceiver = Objects.requireNonNull(audioReceiver);
        this.sendTaskFactory = Objects.requireNonNull(sendTaskFactory);
        this.receiveTaskFactory = Objects.requireNonNull(receiveTaskFactory);
        this.disconnectTask = Objects.requireNonNull(disconnectTask);
        this.voiceSocket = new VoiceSocket(reactorResources.getUdpClient());
        this.heartbeat = new ResettableInterval(reactorResources.getTimerTaskScheduler());

        this.receiverSink = receiver.sink(FluxSink.OverflowStrategy.BUFFER);
        this.outboundSink = outbound.sink(FluxSink.OverflowStrategy.ERROR);
        this.eventSink = events.sink(FluxSink.OverflowStrategy.LATEST);
    }

    public Mono<VoiceConnection> start(String gatewayUrl) {
        return Mono.create(sink -> sink.onRequest(d -> {
            Disposable connect = connect(gatewayUrl, sink)
                    .subscriberContext(sink.currentContext())
                    .subscribe(null,
                            t -> log.error(format(sink.currentContext(), "Voice gateway terminated with an error"), t),
                            () -> log.debug(format(sink.currentContext(), "Voice gateway completed")));
            sink.onCancel(connect);
        }));
    }

    private Mono<Void> connect(String gatewayUrl, MonoSink<VoiceConnection> voiceConnectionSink) {
        return Mono.subscriberContext()
                .flatMap(context -> {
                    disconnectNotifier = MonoProcessor.create();

                    Flux<ByteBuf> outFlux = outbound.flatMap(payloadWriter)
                            .doOnNext(buf -> logPayload(senderLog, context, buf));

                    sessionHandler = new VoiceWebsocketHandler(receiverSink, outFlux, context);

                    // TODO: validate this resume flow
                    if (allowResume.get()) {
                        state.set(VoiceConnection.State.CONNECTING);
                        log.info(format(context, "Attempting to resume"));
                        outboundSink.next(new Resume(Long.toUnsignedString(guildId),
                                Long.toUnsignedString(selfId), sessionId));
                    }

                    Disposable.Composite innerCleanup = Disposables.composite();

                    Mono<Void> receiverFuture = receiver.doOnNext(buf -> logPayload(receiverLog, context, buf))
                            .flatMap(payloadReader)
                            .doOnNext(payload -> {
                                if (!allowResume.get() && payload instanceof Hello) {
                                    state.set(VoiceConnection.State.CONNECTING);
                                    Hello hello = (Hello) payload;
                                    Duration interval = Duration.ofMillis(hello.getData().heartbeatInterval);
                                    heartbeat.start(interval, interval);
                                    log.info(format(context, "Identifying"));
                                    outboundSink.next(new Identify(Long.toUnsignedString(guildId),
                                            Long.toUnsignedString(selfId), sessionId, token));
                                } else if (payload instanceof Ready) {
                                    log.info(format(context, "Waiting for session description"));
                                    Ready ready = (Ready) payload;
                                    ssrc = ready.getData().ssrc;
                                    cleanup.update(innerCleanup);
                                    innerCleanup.add(Mono.defer(() ->
                                            voiceSocket.setup(ready.getData().ip, ready.getData().port))
                                            .then(voiceSocket.performIpDiscovery(ready.getData().ssrc))
                                            .timeout(Duration.ofSeconds(5))
                                            .doOnError(t -> log.warn("Unable to perform voice setup: {}", t.toString()))
                                            .retry()
                                            .subscriberContext(context)
                                            .subscribe(address -> {
                                                        String hostName = address.getHostName();
                                                        int port = address.getPort();
                                                        outboundSink.next(new SelectProtocol(VoiceSocket.PROTOCOL,
                                                                hostName,
                                                                port, VoiceSocket.ENCRYPTION_MODE));
                                                    }, t -> log.error(format(context,
                                                    "Voice socket terminated with an error"), t),
                                                    () -> log.debug(format(context,
                                                            "Voice socket setup completed"))));
                                } else if (payload instanceof SessionDescription) {
                                    log.info(format(context, "Receiving events"));
                                    state.set(VoiceConnection.State.CONNECTED);
                                    connected.set(true);
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
                                    voiceConnectionSink.success(acquireConnection());
                                } else if (payload instanceof Resumed) {
                                    log.info(format(context, "Resumed"));
                                    state.set(VoiceConnection.State.CONNECTED);
                                    connected.set(true);
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
                            .websocket(Integer.MAX_VALUE)
                            .uri(gatewayUrl + "?v=4")
                            .handle(sessionHandler::handle)
                            .subscriberContext(LogUtil.clearContext())
                            .flatMap(t2 -> handleClose(t2.getT1(), t2.getT2()))
                            .then();

                    return Mono.zip(httpFuture, receiverFuture, heartbeatHandler)
                            .doOnError(t -> log.error(format(context, "{}"), t.toString()))
                            .doOnCancel(() -> sessionHandler.close())
                            .then();
                })
                .retryWhen(retryFactory())
                .then(Mono.defer(() -> disconnectNotifier))
                .doOnTerminate(heartbeat::stop);
    }

    private VoiceConnection acquireConnection() {
        return new VoiceConnection() {

            @Override
            public Flux<VoiceGatewayEvent> events() {
                return events;
            }

            @Override
            public boolean isConnected() {
                return connected.get();
            }

            @Override
            public State getState() {
                return state.get();
            }

            @Override
            public Mono<Void> disconnect() {
                return Mono.fromCallable(this::isConnected)
                        .flatMap(connected -> {
                            if (connected) {
                                return stop().then(disconnectTask.onDisconnect(guildId));
                            }
                            return Mono.empty();
                        });
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
        return Retry.<ReconnectContext>onlyIf(t -> isRetryable(t.exception()))
                .withApplicationContext(reconnectContext)
                .withBackoffScheduler(reconnectOptions.getBackoffScheduler())
                .backoff(reconnectOptions.getBackoff())
                .jitter(reconnectOptions.getJitter())
                .retryMax(reconnectOptions.getMaxRetries())
                .doOnRetry(retryContext -> {
                    state.set(VoiceConnection.State.RECONNECTING);
                    connected.set(false);
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

    private boolean isRetryable(Throwable t) {
        if (t instanceof CloseException) {
            CloseException closeException = (CloseException) t;
            return closeException.getCode() != 4004 && closeException.getCode() != 4014;
        }
        return !(t instanceof PartialDisconnectException);
    }

    private boolean canResume(Throwable t) {
        if (t instanceof CloseException) {
            CloseException closeException = (CloseException) t;
            return closeException.getCode() < 4000;
        }
        return true;
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

    private Mono<CloseStatus> handleClose(DisconnectBehavior behavior, CloseStatus closeStatus) {
        return Mono.deferWithContext(ctx -> {
            log.info(format(ctx, "Handling close {} with behavior: {}"), closeStatus, behavior);
            state.set(VoiceConnection.State.DISCONNECTED);
            reconnectContext.clear();
            connected.set(false);

            if (behavior.getAction() == DisconnectBehavior.Action.STOP) {
                allowResume.set(false);
            }

            if (!allowResume.get()) {
                cleanup.dispose();
            }

            switch (behavior.getAction()) {
                case STOP_ABRUPTLY:
                case STOP:
                    disconnectNotifier.onComplete();
                    return Mono.just(closeStatus);
                case RETRY_ABRUPTLY:
                case RETRY:
                default:
                    return Mono.error(new CloseException(closeStatus, ctx, behavior.getCause()));
            }
        });
    }

}
