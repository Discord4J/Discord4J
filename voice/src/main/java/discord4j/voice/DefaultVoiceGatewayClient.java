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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import discord4j.voice.json.ClientsConnect;
import discord4j.voice.json.DaveProtocolExecuteTransition;
import discord4j.voice.json.DaveProtocolPrepareEpoch;
import discord4j.voice.json.DaveProtocolPrepareTransition;
import discord4j.voice.json.DaveProtocolReadyForTransition;
import discord4j.voice.json.Heartbeat;
import discord4j.voice.json.HeartbeatAck;
import discord4j.voice.json.Hello;
import discord4j.voice.json.Identify;
import discord4j.voice.json.MlsInvalidCommitWelcome;
import discord4j.voice.json.Ready;
import discord4j.voice.json.Resume;
import discord4j.voice.json.Resumed;
import discord4j.voice.json.SelectProtocol;
import discord4j.voice.json.SentSpeaking;
import discord4j.voice.json.SessionDescription;
import discord4j.voice.json.Speaking;
import discord4j.voice.json.VoiceDisconnect;
import discord4j.voice.json.VoiceGatewayPayload;
import discord4j.voice.retry.VoiceGatewayException;
import discord4j.voice.retry.VoiceGatewayReconnectException;
import discord4j.voice.retry.VoiceGatewayRetrySpec;
import discord4j.voice.retry.VoiceServerUpdateReconnectException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Sinks;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
    private final ObjectMapper mapper;
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
     * Frames coming from the websocket.
     */
    private final Sinks.Many<VoiceGatewayFrame> receiver;

    /**
     * Outbound voice gateway frames.
     */
    private final Sinks.Many<VoiceGatewayFrame> outbound;

    /**
     * Inbound voice gateway events that are pushed to consumers.
     */
    private final Sinks.Many<VoiceGatewayEvent> events;

    /**
     * Internal connection state changes are reflected as emissions to this sink.
     */
    private final Sinks.Many<VoiceConnection.State> state;

    private final AtomicReference<@Nullable VoiceServerOptions> serverOptions = new AtomicReference<>();
    private final AtomicReference<@Nullable Snowflake> currentChannelId = new AtomicReference<>();
    private final AtomicReference<@Nullable String> session = new AtomicReference<>();
    private final AtomicReference<@Nullable DaveProtocolSession> daveSession = new AtomicReference<>();
    private final ConcurrentMap<Integer, Long> ssrcToUserId = new ConcurrentHashMap<Integer, Long>();
    private final ConcurrentMap<Long, Integer> userIdToSsrc = new ConcurrentHashMap<Long, Integer>();

    private volatile int ssrc;
    private volatile long gatewaySequence;
    private volatile Sinks.@Nullable One<CloseStatus> disconnectNotifier;
    private volatile @Nullable ContextView currentContext;
    private volatile @Nullable VoiceWebsocketHandler sessionHandler;
    private volatile @Nullable EncryptionMode encryptionMode;

    public DefaultVoiceGatewayClient(VoiceGatewayOptions options) {
        this.guildId = options.getGuildId();
        this.selfId = options.getSelfId();
        this.mapper = Objects.requireNonNull(options.getJacksonResources()).getObjectMapper();
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

    public Mono<VoiceConnection> start(VoiceServerOptions voiceServerOptions, String sessionId) {
        return Mono.create(sink -> sink.onRequest(d -> {

            Disposable.Composite outerCleanup = Disposables.composite();

            outerCleanup.add(serverUpdateTask.onVoiceServerUpdates(guildId)
                    .subscribe(newValue -> {
                        VoiceServerOptions current = serverOptions.get();
                        if (current != null && !current.getEndpoint().equals(newValue.getEndpoint())) {
                            log.debug(format(sink.contextView(), "Voice server endpoint change: {} -> {}"),
                                    current.getEndpoint(), newValue.getEndpoint());
                            serverOptions.set(newValue);
                            VoiceWebsocketHandler sessionHandlerToClose = sessionHandler;
                            if (sessionHandlerToClose != null) {
                                sessionHandlerToClose.close(DisconnectBehavior.retryAbruptly(
                                        new VoiceServerUpdateReconnectException(sink.contextView())));
                            }
                        }
                    }));

            outerCleanup.add(initializeChannelId()
                    .flatMap(ignore -> connect(voiceServerOptions, sessionId, sink))
                    .contextWrite(sink.contextView())
                    .subscribe(null,
                            t -> log.debug(format(sink.contextView(), "Voice gateway error: {}"), t.toString()),
                            () -> log.debug(format(sink.contextView(), "Voice gateway completed"))
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

                            Flux<VoiceGatewayFrame> outFlux = outbound.asFlux()
                                    .doOnNext(frame -> logPayload(senderLog, context, frame));

                            sessionHandler = new VoiceWebsocketHandler(receiver, outFlux, context);

                            Mono<?> onOpen = state.asFlux()
                                    .next()
                                    .doOnNext(s -> {
                                        if (s == VoiceConnection.State.RESUMING) {
                                            log.info(format(context, "Attempting to resume"));
                                            emitJson(new Resume(guildId.asString(),
                                                    Objects.requireNonNull(session.get()),
                                                    Objects.requireNonNull(serverOptions.get()).getToken(),
                                                    Long.valueOf(gatewaySequence)));
                                        } else {
                                            nextState(VoiceConnection.State.CONNECTING);
                                            log.info(format(context, "Identifying"));
                                            emitJson(new Identify(guildId.asString(),
                                                    selfId.asString(), Objects.requireNonNull(session.get()),
                                                    Objects.requireNonNull(serverOptions.get()).getToken(),
                                                    DaveProtocolSession.getMaxSupportedProtocolVersion()));
                                        }
                                    });

                            Disposable.Composite innerCleanup = Disposables.composite();

                            Mono<Void> receiverFuture = receiver.asFlux()
                                    .doOnNext(frame -> logPayload(receiverLog, context, frame))
                                    .flatMap(frame -> Mono.fromCallable(() -> decodeGatewayFrame(frame))
                                            .flatMap(Mono::justOrEmpty))
                                    .doOnNext(event -> handleEvent(event, innerCleanup, voiceConnectionSink, context))
                                    .then();

                            Mono<Void> heartbeatHandler = heartbeat.ticks()
                                    .map(Heartbeat::new)
                                    .doOnNext(this::emitJson)
                                    .then();

                            String fullEndpoint = Objects.requireNonNull(serverOptions.get()).getEndpoint();
                            log.debug("Using endpoint {}", fullEndpoint);

                            Mono<Void> httpFuture = httpClient
                                    .websocket(WebsocketClientSpec.builder()
                                            .maxFramePayloadLength(Integer.MAX_VALUE)
                                            .build())
                                    .uri(fullEndpoint)
                                    .handle((in, out) -> onOpen.then(Objects.requireNonNull(sessionHandler).handle(in, out)))
                                    .contextWrite(LogUtil.clearContext())
                                    .flatMap(t2 -> handleClose(t2.getT1(), t2.getT2()))
                                    .then();

                            return Mono.zip(httpFuture, receiverFuture, heartbeatHandler)
                                    .doOnError(t -> log.error(format(context, "{}"), t.toString()))
                                    .doOnTerminate(heartbeat::stop)
                                    .doOnCancel(() -> Objects.requireNonNull(sessionHandler).close())
                                    .then();
                        })
                .contextWrite(ctx -> ctx.put(LogUtil.KEY_GUILD_ID, guildId.asString()))
                .retryWhen(retryFactory())
                .then(Mono.defer(() -> Objects.requireNonNull(disconnectNotifier).asMono().then()))
                .doOnSubscribe(s -> {
                    if (disconnectNotifier != null) {
                        throw new IllegalStateException("connect can only be subscribed once");
                    }
                });
    }

    private Mono<Snowflake> initializeChannelId() {
        Snowflake existing = currentChannelId.get();
        if (existing != null) {
            return Mono.just(existing);
        }

        return channelRetrieveTask.onRequest()
                .switchIfEmpty(Mono.error(new IllegalStateException("Unable to retrieve voice channel ID for DAVE")))
                .doOnNext(channelId -> currentChannelId.compareAndSet(null, channelId));
    }

    private DaveProtocolSession getDaveSession() {
        return Objects.requireNonNull(daveSession.get(), "DAVE session has not been initialized");
    }

    private DaveProtocolSession initializeDaveSession(@Nullable String authSessionId) {
        DaveProtocolSession existing = daveSession.get();
        if (existing != null) {
            return existing;
        }

        Snowflake channelId = Objects.requireNonNull(currentChannelId.get(),
                "Voice channel ID has not been initialized");
        DaveProtocolSession created = DaveProtocolSession.create(selfId.asLong(), channelId.asLong(),
                authSessionId, new DaveGatewayCallbacks() {
                    @Override
                    public void sendMlsKeyPackage(byte[] mlsKeyPackage) {
                        emitBinary(26, mlsKeyPackage);
                    }

                    @Override
                    public void sendDaveProtocolReadyForTransition(int transitionId) {
                        emitJson(new DaveProtocolReadyForTransition(transitionId));
                    }

                    @Override
                    public void sendMlsCommitWelcome(byte[] commitWelcomeMessage) {
                        emitBinary(28, commitWelcomeMessage);
                    }

                    @Override
                    public void sendMlsInvalidCommitWelcome(int transitionId) {
                        emitJson(new MlsInvalidCommitWelcome(transitionId));
                    }
                });
        if (!daveSession.compareAndSet(null, created)) {
            created.close();
        }
        return Objects.requireNonNull(daveSession.get());
    }

    private void destroyDaveSession() {
        DaveProtocolSession sessionToClose = daveSession.getAndSet(null);
        if (sessionToClose != null) {
            sessionToClose.close();
        }
        ssrcToUserId.clear();
        userIdToSsrc.clear();
        currentChannelId.set(null);
    }

    private void handleEvent(VoiceGatewayEvent event, Disposable.Composite innerCleanup,
                             MonoSink<VoiceConnection> voiceConnectionSink, ContextView context) {
        if (event instanceof Hello) {
            Hello hello = (Hello) event;
            Duration interval = Duration.ofMillis(hello.getData().getHeartbeatInterval());
            heartbeat.start(interval, interval);
        } else if (event instanceof Ready) {
            log.info(format(context, "Waiting for session description"));
            Ready ready = (Ready) event;
            initializeDaveSession(ready.getData().getAuthSessionId());
            ssrc = ready.getData().getSsrc();
            cleanup.update(innerCleanup);
            innerCleanup.add(Mono.defer(() ->
                            voiceSocket.setup(ready.getData().getIp(), ready.getData().getPort()))
                    .zipWith(voiceSocket.performIpDiscovery(ready.getData().getSsrc()))
                    .timeout(ipDiscoveryTimeout)
                    .retryWhen(ipDiscoveryRetrySpec)
                    .contextWrite(context)
                    .onErrorMap(t -> new VoiceGatewayException(context, "UDP socket setup error", t))
                    .subscribe(TupleUtils.consumer((connection, address) -> {
                                innerCleanup.add(connection);
                                String hostName = address.getHostName();
                                int port = address.getPort();

                                this.encryptionMode = EncryptionMode.getBestMode(ready.getData().getModes());
                                if (this.encryptionMode == null) {
                                    nextState(VoiceConnection.State.DISCONNECTED);
                                    voiceConnectionSink.error(new IllegalStateException(
                                            "No mutually supported encryption mode available"));
                                    return;
                                }

                                getDaveSession().assignOpusSsrc(ssrc);
                                log.info("Using encryption mode {}", this.encryptionMode.name());

                                emitJson(new SelectProtocol(VoiceSocket.PROTOCOL, hostName, port,
                                        encryptionMode.getValue()));
                            }),
                            t -> {
                                voiceConnectionSink.error(t);
                                Objects.requireNonNull(sessionHandler).close(DisconnectBehavior.stop(t));
                            },
                            () -> log.debug(format(context, "Voice socket setup complete"))));
        } else if (event instanceof SessionDescription) {
            log.info(format(context, "Receiving events"));
            nextState(VoiceConnection.State.CONNECTED);
            reconnectContext.reset();
            SessionDescription sessionDescription = (SessionDescription) event;
            byte[] secretKey = sessionDescription.getData().getSecretKey();

            PacketTransformer transformer;
            try {
                transformer = new PacketTransformer(ssrc,
                        Objects.requireNonNull(encryptionMode),
                        secretKey,
                        getDaveSession(),
                        ssrcToUserId::get);
            } catch (GeneralSecurityException e) {
                log.error("Failed to create packet transformer", e);
                nextState(VoiceConnection.State.DISCONNECTED);
                voiceConnectionSink.error(e);
                return;
            }

            getDaveSession().onSelectProtocolAck(sessionDescription.getData().getDaveProtocolVersion());
            emitJson(new SentSpeaking(false, 0, ssrc));

            Consumer<Boolean> speakingSender = speaking -> emitJson(new SentSpeaking(speaking, 0, ssrc));
            innerCleanup.add(() -> log.debug(format(context, "Disposing voice tasks")));
            innerCleanup.add(sendTaskFactory.create(reactorResources.getSendTaskScheduler(),
                    speakingSender, voiceSocket::send, audioProvider, transformer));
            innerCleanup.add(receiveTaskFactory.create(reactorResources.getReceiveTaskScheduler(),
                    voiceSocket.getInbound(), transformer, audioReceiver));
            voiceConnectionSink.success(acquireConnection());
        } else if (event instanceof Resumed) {
            log.info(format(context, "Resumed"));
            nextState(VoiceConnection.State.CONNECTED);
            reconnectContext.reset();
        } else if (event instanceof Speaking) {
            Speaking speaking = (Speaking) event;
            long userId = Long.parseUnsignedLong(speaking.getData().getUserId());
            rememberUserSsrc(userId, speaking.getData().getSsrc());
            getDaveSession().addUser(userId);
        } else if (event instanceof ClientsConnect) {
            ClientsConnect clientsConnect = (ClientsConnect) event;
            for (String userId : clientsConnect.getData().getUserIds()) {
                getDaveSession().addUser(Long.parseUnsignedLong(userId));
            }
        } else if (event instanceof VoiceDisconnect) {
            VoiceDisconnect disconnect = (VoiceDisconnect) event;
            long userId = Long.parseUnsignedLong(disconnect.getData().getUserId());
            forgetUser(userId);
            getDaveSession().removeUser(userId);
        } else if (event instanceof DaveProtocolPrepareTransition) {
            DaveProtocolPrepareTransition payload = (DaveProtocolPrepareTransition) event;
            getDaveSession().onDaveProtocolPrepareTransition(payload.getData().getTransitionId(),
                    payload.getData().getProtocolVersion());
        } else if (event instanceof DaveProtocolExecuteTransition) {
            DaveProtocolExecuteTransition payload = (DaveProtocolExecuteTransition) event;
            getDaveSession().onDaveProtocolExecuteTransition(payload.getData().getTransitionId());
        } else if (event instanceof DaveProtocolPrepareEpoch) {
            DaveProtocolPrepareEpoch payload = (DaveProtocolPrepareEpoch) event;
            getDaveSession().onDaveProtocolPrepareEpoch(payload.getData().getEpoch(),
                    payload.getData().getProtocolVersion());
        } else if (event instanceof DaveMlsExternalSenderPackage) {
            getDaveSession().onDaveProtocolMlsExternalSenderPackage(
                    ((DaveMlsExternalSenderPackage) event).getExternalSenderPackage());
        } else if (event instanceof DaveMlsProposals) {
            getDaveSession().onMlsProposals(((DaveMlsProposals) event).getProposals());
        } else if (event instanceof DaveMlsAnnounceCommitTransition) {
            DaveMlsAnnounceCommitTransition payload = (DaveMlsAnnounceCommitTransition) event;
            getDaveSession().onMlsPrepareCommitTransition(payload.getTransitionId(), payload.getCommitMessage());
        } else if (event instanceof DaveMlsWelcome) {
            DaveMlsWelcome payload = (DaveMlsWelcome) event;
            getDaveSession().onMlsWelcome(payload.getTransitionId(), payload.getWelcomeMessage());
        }

        emissionStrategy.emitNext(events, event);
    }

    private @Nullable VoiceGatewayEvent decodeGatewayFrame(VoiceGatewayFrame frame) throws Exception {
        return frame.isBinary() ? decodeBinaryFrame(frame.getContent()) : decodeTextFrame(frame.getContent());
    }

    private @Nullable VoiceGatewayEvent decodeTextFrame(ByteBuf buf) throws Exception {
        JsonNode json = mapper.readTree(ByteBufUtil.getBytes(buf));
        if (json.has("seq")) {
            gatewaySequence = json.get("seq").asLong();
        }

        int op = json.get("op").asInt();
        JsonNode d = json.get("d");
        switch (op) {
            case Hello.OP:
                return new Hello(d.get("heartbeat_interval").asLong());
            case Ready.OP:
                return new Ready(d.get("ssrc").asInt(), d.get("ip").asText(), d.get("port").asInt(),
                        readStringArray(d.get("modes")),
                        d.has("auth_session_id") && !d.get("auth_session_id").isNull()
                                ? d.get("auth_session_id").asText()
                                : null);
            case HeartbeatAck.OP:
                return new HeartbeatAck(d.asLong());
            case SessionDescription.OP:
                ArrayNode secretKeyNode = (ArrayNode) d.get("secret_key");
                byte[] secretKey = mapper.readValue(secretKeyNode.traverse(mapper), byte[].class);
                return new SessionDescription(d.get("mode").asText(), secretKey,
                        d.has("dave_protocol_version") ? d.get("dave_protocol_version").asInt() : 0);
            case Speaking.OP:
                return new Speaking(d.get("user_id").asText(), d.get("ssrc").asInt(), d.get("speaking").asBoolean());
            case ClientsConnect.OP:
                return new ClientsConnect(readStringArray(d.get("user_ids")));
            case VoiceDisconnect.OP:
                return new VoiceDisconnect(d.get("user_id").asText());
            case Resumed.OP:
                return new Resumed(d != null ? d.asText() : null);
            case DaveProtocolPrepareTransition.OP:
                return new DaveProtocolPrepareTransition(d.get("transition_id").asInt(),
                        d.get("protocol_version").asInt());
            case DaveProtocolExecuteTransition.OP:
                return new DaveProtocolExecuteTransition(d.get("transition_id").asInt());
            case DaveProtocolPrepareEpoch.OP:
                return new DaveProtocolPrepareEpoch(d.get("epoch").asLong(),
                        d.get("protocol_version").asInt());
            default:
                log.debug("Received voice gateway payload with unhandled OP: {}", op);
                return null;
        }
    }

    private @Nullable VoiceGatewayEvent decodeBinaryFrame(ByteBuf buf) {
        if (buf.readableBytes() < 1) {
            log.debug("Received truncated binary voice gateway frame");
            return null;
        }

        int firstByte = buf.getUnsignedByte(buf.readerIndex());
        if (isDaveBinaryOpcode(firstByte)) {
            return decodeOpcodePrefixedBinaryFrame(buf);
        }

        if (buf.readableBytes() < 3) {
            log.debug("Received truncated legacy binary voice gateway frame");
            return null;
        }

        int sequenceNumber = buf.readUnsignedShort();
        gatewaySequence = sequenceNumber;
        int opcode = buf.readUnsignedByte();

        switch (opcode) {
            case 25:
                return new DaveMlsExternalSenderPackage(sequenceNumber, readRemaining(buf));
            case 27:
                return new DaveMlsProposals(sequenceNumber, readRemaining(buf));
            case 29:
                if (buf.readableBytes() < 2) {
                    log.debug("Received truncated MLS announce commit transition frame");
                    return null;
                }
                return new DaveMlsAnnounceCommitTransition(sequenceNumber, buf.readUnsignedShort(), readRemaining(buf));
            case 30:
                if (buf.readableBytes() < 2) {
                    log.debug("Received truncated MLS welcome frame");
                    return null;
                }
                return new DaveMlsWelcome(sequenceNumber, buf.readUnsignedShort(), readRemaining(buf));
            default:
                log.debug("Received binary voice gateway payload with unhandled OP: {}", opcode);
                return null;
        }
    }

    private @Nullable VoiceGatewayEvent decodeOpcodePrefixedBinaryFrame(ByteBuf buf) {
        int opcode = buf.readUnsignedByte();

        switch (opcode) {
            case 25:
                return new DaveMlsExternalSenderPackage(-1, readRemaining(buf));
            case 27:
                return new DaveMlsProposals(-1, readRemaining(buf));
            case 29:
                return decodeOpcodePrefixedTransitionFrame(buf, true);
            case 30:
                return decodeOpcodePrefixedTransitionFrame(buf, false);
            default:
                log.debug("Received opcode-prefixed binary voice gateway payload with unhandled OP: {}", opcode);
                return null;
        }
    }

    private @Nullable VoiceGatewayEvent decodeOpcodePrefixedTransitionFrame(ByteBuf buf, boolean commit) {
        if (buf.readableBytes() < 2) {
            log.debug("Received truncated opcode-prefixed {} frame", commit ? "commit transition" : "welcome");
            return null;
        }

        int transitionId = buf.readUnsignedShort();
        byte[] payload = readRemaining(buf);
        return commit
                ? new DaveMlsAnnounceCommitTransition(-1, transitionId, payload)
                : new DaveMlsWelcome(-1, transitionId, payload);
    }

    private static List<String> readStringArray(@Nullable JsonNode node) {
        List<String> values = new ArrayList<String>();
        if (node == null) {
            return values;
        }
        for (JsonNode child : node) {
            values.add(child.asText());
        }
        return values;
    }

    private static byte[] readRemaining(ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    private static boolean isDaveBinaryOpcode(int value) {
        switch (value) {
            case 25:
            case 27:
            case 29:
            case 30:
                return true;
            default:
                return false;
        }
    }

    private void rememberUserSsrc(long userId, int userSsrc) {
        Integer previousSsrc = userIdToSsrc.put(userId, userSsrc);
        if (previousSsrc != null && previousSsrc.intValue() != userSsrc) {
            ssrcToUserId.remove(previousSsrc);
        }
        ssrcToUserId.put(userSsrc, userId);
    }

    private void forgetUser(long userId) {
        Integer previousSsrc = userIdToSsrc.remove(userId);
        if (previousSsrc != null) {
            ssrcToUserId.remove(previousSsrc);
        }
    }

    private void emitJson(VoiceGatewayPayload<?> payload) {
        try {
            emissionStrategy.emitNext(outbound, VoiceGatewayFrame.text(
                    Unpooled.wrappedBuffer(mapper.writeValueAsBytes(payload))));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encode voice gateway payload", e);
        }
    }

    private void emitBinary(int opcode, byte[] payload) {
        ByteBuf frame = Unpooled.buffer(Byte.BYTES + payload.length)
                .writeByte(opcode)
                .writeBytes(payload);
        emissionStrategy.emitNext(outbound, VoiceGatewayFrame.binary(frame));
    }

    private void nextState(VoiceConnection.State value) {
        log.debug(format(Objects.requireNonNull(currentContext), "New state: {}"), value);
        emissionStrategy.emitNext(state, value);
    }

    private VoiceConnection acquireConnection() {
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
                                Mono.fromRunnable(() -> Objects.requireNonNull(sessionHandler).close(
                                                DisconnectBehavior.retryAbruptly(
                                                        errorCause.apply(Objects.requireNonNull(currentContext)))))
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
            final VoiceWebsocketHandler sessionHandlerToClose = sessionHandler;
            final Sinks.One<CloseStatus> disconnectNotifierCopy = disconnectNotifier;
            if (sessionHandlerToClose == null || disconnectNotifierCopy == null) {
                return Mono.error(new IllegalStateException("Gateway client is not active!"));
            }
            sessionHandlerToClose.close(DisconnectBehavior.stop(null));
            return disconnectNotifierCopy.asMono().then();
        });
    }

    private void logPayload(Logger logger, ContextView context, VoiceGatewayFrame frame) {
        if (!logger.isTraceEnabled()) {
            return;
        }

        if (frame.isBinary()) {
            logger.trace(format(context, ByteBufUtil.hexDump(frame.getContent())));
            return;
        }

        logger.trace(format(context, frame.getContent().toString(StandardCharsets.UTF_8)
                .replaceAll("(\"token\": ?\")([A-Za-z0-9._-]*)(\")", "$1hunter2$3")));
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
                behavior = DisconnectBehavior.stop(sourceBehavior.getCause());
            } else {
                behavior = sourceBehavior;
            }
            log.debug(format(ctx, "Closing and {} with status {}"), behavior, closeStatus);
            heartbeat.stop();

            if (behavior.getAction() == DisconnectBehavior.Action.STOP
                    || behavior.getAction() == DisconnectBehavior.Action.STOP_ABRUPTLY) {
                cleanup.dispose();
                destroyDaveSession();
            }

            switch (behavior.getAction()) {
                case STOP_ABRUPTLY:
                case STOP:
                    if (behavior.getCause() != null) {
                        return Mono.just(new CloseException(closeStatus, ctx, behavior.getCause()))
                                .flatMap(ex -> {
                                    nextState(VoiceConnection.State.DISCONNECTED);
                                    Objects.requireNonNull(disconnectNotifier).emitError(ex, FAIL_FAST);
                                    Mono<CloseStatus> thenMono = closeStatus.getCode() == 4014 ?
                                            Mono.just(closeStatus) : Mono.error(ex);
                                    return disconnectTask.onDisconnect(guildId).then(thenMono);
                                });
                    }
                    return Mono.just(closeStatus)
                            .flatMap(status -> {
                                nextState(VoiceConnection.State.DISCONNECTED);
                                Objects.requireNonNull(disconnectNotifier).emitValue(closeStatus, FAIL_FAST);
                                return disconnectTask.onDisconnect(guildId).thenReturn(closeStatus);
                            });
                case RETRY_ABRUPTLY:
                case RETRY:
                default:
                    return Mono.error(new CloseException(closeStatus, ctx, behavior.getCause()));
            }
        });
    }

}
