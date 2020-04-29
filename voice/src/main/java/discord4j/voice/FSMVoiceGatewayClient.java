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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.voice;

import com.discord4j.fsm.FiniteStateMachine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwebpp.crypto.TweetNaclFast;
import discord4j.voice.VoiceGatewayEvent.Start;
import discord4j.voice.VoiceGatewayEvent.Stop;
import discord4j.voice.VoiceGatewayState.*;
import discord4j.voice.json.*;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.InputStream;
import java.time.Duration;

import static io.netty.handler.codec.http.HttpHeaderNames.USER_AGENT;

public class FSMVoiceGatewayClient {

    private static final Logger log = Loggers.getLogger(FSMVoiceGatewayClient.class);
    private final FiniteStateMachine<VoiceGatewayState, VoiceGatewayEvent> gatewayFSM;

    private final EmitterProcessor<VoiceGatewayPayload<?>> sender = EmitterProcessor.create(false);
    private final ObjectMapper mapper;
    final VoiceSocket voiceSocket;

    public FSMVoiceGatewayClient(long serverId, long userId, String sessionId, String token,
                                 VoiceReactorResources reactorResources, ObjectMapper mapper,
                                 AudioProvider provider, AudioReceiver receiver,
                                 VoiceSendTaskFactory sendTaskFactory, VoiceReceiveTaskFactory receiveTaskFactory,
                                 VoiceDisconnectTask disconnectTask) {
        this.mapper = mapper;
        this.voiceSocket = new VoiceSocket(reactorResources.getUdpClient());
        this.gatewayFSM = new FiniteStateMachine<VoiceGatewayState, VoiceGatewayEvent>() {{
            startWith(Stopped.INSTANCE);

            when(Stopped.class)
                    .on(Start.class, (curState, start) -> {
                        Disposable websocketTask = HttpClient.create()
                                .wiretap(true)
                                .headers(headers -> headers.add(USER_AGENT, "DiscordBot(https://discord4j.com, 3)"))
                                .websocket(WebsocketClientSpec.builder()
                                        .maxFramePayloadLength(Integer.MAX_VALUE)
                                        .build())
                                .uri(start.gatewayUrl + "?v=3")
                                .handle(FSMVoiceGatewayClient.this::handle)
                                .subscribe();

                        log.debug("VoiceGateway State Change: Stopped -> WaitingForHello");
                        return new WaitingForHello(websocketTask, start.connectedCallback);
                    });

            when(WaitingForHello.class)
                    .on(Hello.class, (curState, hello) -> {
                        long heartbeatInterval = (long) (hello.getData().heartbeatInterval * .75); // it's wrong
                        Disposable heartbeatTask = Flux.interval(Duration.ofMillis(heartbeatInterval),
                                Schedulers.elastic())
                                .map(Heartbeat::new)
                                .subscribe(FSMVoiceGatewayClient.this::send);

                        send(new Identify(Long.toUnsignedString(serverId), Long.toUnsignedString(userId), sessionId,
                                token));

                        log.debug("VoiceGateway State Change: WaitingForHello -> WaitingForReady");
                        return new WaitingForReady(curState.websocketTask, curState.connectedCallback, heartbeatTask);
                    });

            when(WaitingForReady.class)
                    .on(Ready.class, (curState, ready) -> {
                        int ssrc = ready.getData().ssrc;

                        Disposable udpTask = voiceSocket.setup(ready.getData().ip, ready.getData().port)
                                .then(voiceSocket.performIpDiscovery(ssrc))
                                .subscribe(ipaddr -> {
                                    String address = ipaddr.getHostName();
                                    int port = ipaddr.getPort();
                                    send(new SelectProtocol(VoiceSocket.PROTOCOL, address, port,
                                            VoiceSocket.ENCRYPTION_MODE));
                                });

                        log.debug("VoiceGateway State Change: WaitingForReady -> WaitingForSessionDescription");
                        return new WaitingForSessionDescription(curState.websocketTask, curState.connectedCallback,
                                curState.heartbeatTask, ssrc, udpTask);
                    });

            when(WaitingForSessionDescription.class)
                    .on(SessionDescription.class, (curState, sessionDesc) -> {
                        byte[] secretKey = sessionDesc.getData().secretKey;
                        TweetNaclFast.SecretBox boxer = new TweetNaclFast.SecretBox(secretKey);
                        PacketTransformer transformer = new PacketTransformer(curState.ssrc, boxer);

                        Disposable sendingTask = sendTaskFactory.create(reactorResources.getSendTaskScheduler(),
                                speaking -> send(new SentSpeaking(speaking, 0, curState.ssrc)),
                                voiceSocket::send, provider, transformer);
                        Disposable receivingTask = receiveTaskFactory.create(reactorResources.getReceiveTaskScheduler(),
                                voiceSocket.getInbound(), transformer, receiver);

                        // we're completely connected
                        curState.connectedCallback.success(acquireConnection(disconnectTask, serverId));

                        log.debug("VoiceGateway State Change: WaitingForSessionDescription -> ReceivingEvents");
                        return new ReceivingEvents(curState.websocketTask/*, curState.connectedCallback*/,
                                curState.heartbeatTask, curState.ssrc, curState.udpTask, secretKey, sendingTask,
                                receivingTask);
                    });

            when(ReceivingEvents.class)
                    .on(Stop.class, (curState, stop) -> {
                        // clean up running tasks
                        curState.heartbeatTask.dispose();
                        curState.sendingTask.dispose();
                        curState.receivingTask.dispose();
                        curState.udpTask.dispose();

                        log.debug("VoiceGateway State Change: ReceivingEvents -> Stopped");
                        return Stopped.INSTANCE;
                    });

            whenAny()
                    .on(HeartbeatAck.class, (curState, ack) -> {
                        // TODO
                        return curState;
                    })
                    .on(Speaking.class, (curState, speaking) -> {
                        // TODO
                        return curState;
                    })
                    .on(VoiceDisconnect.class, (curState, voiceDisconnect) -> {
                        // TODO
                        return curState;
                    });
        }};
    }

    void start(String gatewayUrl, MonoSink<VoiceConnection> voiceConnectionSink) {
        gatewayFSM.onEvent(new Start(gatewayUrl, voiceConnectionSink));
    }

    void stop() {
        gatewayFSM.onEvent(new Stop());
    }

    private Mono<Void> handle(WebsocketInbound in, WebsocketOutbound out) {
        Mono<Void> inboundThen = in.aggregateFrames()
                .receiveFrames()
                .map(WebSocketFrame::content)
                .flatMap(buf -> Mono.fromCallable(() ->
                        mapper.readValue((InputStream) new ByteBufInputStream(buf), VoiceGatewayPayload.class)))
                .doOnNext(gatewayFSM::onEvent)
                .then();

        Mono<Void> outboundThen = out.sendObject(sender.flatMap(payload -> Mono.fromCallable(() ->
                new TextWebSocketFrame(Unpooled.wrappedBuffer(mapper.writeValueAsBytes(payload))))))
                .then();

        return Mono.zip(inboundThen, outboundThen).then();
    }

    <T> void send(VoiceGatewayPayload<T> payload) {
        sender.onNext(payload);
    }

    private VoiceConnection acquireConnection(VoiceDisconnectTask disconnectTask, long guildId) {
        return new VoiceConnection() {

            @Override
            public Flux<VoiceGatewayEvent> events() {
                // unsupported
                return Flux.empty();
            }

            @Override
            public boolean isConnected() {
                return gatewayFSM.getCurrentState() instanceof ReceivingEvents;
            }

            @Override
            public State getState() {
                if (gatewayFSM.getCurrentState() instanceof ReceivingEvents) {
                    return State.CONNECTED;
                } else if (gatewayFSM.getCurrentState() instanceof Stopped) {
                    return State.DISCONNECTED;
                } else {
                    // TODO: this implementation is unable to reconnect yet
                    return State.CONNECTING;
                }
            }

            @Override
            public Mono<Void> disconnect() {
                return Mono.fromCallable(this::isConnected)
                        .flatMap(connected -> {
                            if (connected) {
                                return Mono.fromRunnable(() -> stop())
                                        .then(disconnectTask.onDisconnect(guildId));
                            }
                            return Mono.empty();
                        });
            }
        };
    }
}
