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

import com.darichey.simplefsm.FiniteStateMachine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwebpp.crypto.TweetNaclFast;
import discord4j.voice.VoiceGatewayState.ReceivingEvents;
import discord4j.voice.VoiceGatewayState.WaitingForHello;
import discord4j.voice.VoiceGatewayState.WaitingForReady;
import discord4j.voice.VoiceGatewayState.WaitingForSessionDescription;
import discord4j.voice.json.*;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.netty.NettyPipeline;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaderNames.USER_AGENT;

public class VoiceGatewayClient {

    private final Logger log = Loggers.getLogger("discord4j.voice.gateway.client");
    private final FiniteStateMachine<VoiceGatewayState, VoiceGatewayPayload<?>> gatewayFSM;

    private final EmitterProcessor<VoiceGatewayPayload<?>> sender = EmitterProcessor.create(false);
    private final ObjectMapper mapper;
    final VoiceSocket voiceSocket;

    public VoiceGatewayClient(long serverId, long userId, String sessionId, String token, ObjectMapper mapper,
                              Scheduler scheduler, AudioProvider provider) {
        this.mapper = mapper;
        this.voiceSocket = new VoiceSocket();
        this.gatewayFSM = new FiniteStateMachine<VoiceGatewayState, VoiceGatewayPayload<?>>() {{
            startWith(WaitingForHello.INSTANCE);

            when(WaitingForHello.class)
                    .on(Hello.class, (curState, hello) -> {
                        long heartbeatInterval = (long) (hello.getData().heartbeatInterval * .75);
                        Disposable heartbeat = Flux.interval(Duration.ofMillis(heartbeatInterval))
                                .map(Heartbeat::new)
                                .subscribe(VoiceGatewayClient.this::send);

                        send(new Identify(Long.toUnsignedString(serverId), Long.toUnsignedString(userId), sessionId, token));
                        return new WaitingForReady(heartbeat);
                    });

            when(WaitingForReady.class)
                    .on(Ready.class, (curState, ready) -> {
                        int ssrc = ready.getData().ssrc;

                        voiceSocket.setup(ready.getData().ip, ready.getData().port)
                                .then(voiceSocket.performIpDiscovery(ssrc))
                                .subscribe(ipaddr -> {
                                    String address = ipaddr.getHostName();
                                    int port = ipaddr.getPort();
                                    send(new SelectProtocol(VoiceSocket.PROTOCOL, address, port, VoiceSocket.ENCRYPTION_MODE));
                                });

                        return new WaitingForSessionDescription(curState.getHeartbeat(), ssrc);
                    });

            when(WaitingForSessionDescription.class)
                    .on(SessionDescription.class, (curState, sessionDesc) -> {
                        TweetNaclFast.SecretBox boxer = new TweetNaclFast.SecretBox(sessionDesc.getData().secretKey);
                        PacketTransformer transformer = new PacketTransformer(curState.getSsrc(), boxer);

                        VoiceSendTask sendTask = new VoiceSendTask(VoiceGatewayClient.this, provider, transformer, curState.getSsrc());
                        Disposable sending = scheduler.schedulePeriodically(sendTask, 0, Opus.FRAME_TIME, TimeUnit.MILLISECONDS);

                        return new ReceivingEvents(curState.getHeartbeat(), curState.getSsrc(), sessionDesc.getData().secretKey, sending);
                    });

            whenAny()
                    .on(HeartbeatAck.class, (curState, ack) -> {
                        // TODO
                        return curState;
                    }).on(Speaking.class, (curState, speaking) -> {
                        // TODO
                        return curState;
                    })
                    .on(VoiceDisconnect.class, (curState, voiceDisconnect) -> {
                        // TODO
                        return curState;
                    });
        }};
    }

    public Mono<Void> execute(String gatewayUrl) {
        return HttpClient.create()
                .wiretap(true)
                .headers(headers -> headers.add(USER_AGENT, "DiscordBot(https://discord4j.com, 3)"))
                .websocket(Integer.MAX_VALUE)
                .uri(gatewayUrl + "?v=3")
                .handle(this::handle)
                .then();
    }

    private Mono<Void> handle(WebsocketInbound in, WebsocketOutbound out) {
        Mono<Void> inboundThen = in.aggregateFrames()
                .receiveFrames()
                .map(WebSocketFrame::content)
                .flatMap(buf -> Mono.fromCallable(() -> mapper.readValue((InputStream) new ByteBufInputStream(buf), VoiceGatewayPayload.class)))
                .doOnNext(gatewayFSM::onEvent)
                .then();

        Mono<Void> outboundThen = out.options(NettyPipeline.SendOptions::flushOnEach)
                .sendObject(sender.map(payload -> {
                    try {
                        return new TextWebSocketFrame(Unpooled.wrappedBuffer(mapper.writeValueAsBytes(payload)));
                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }
                }))
                .then();

        return Mono.zip(inboundThen, outboundThen).then();
    }

    <T> void send(VoiceGatewayPayload<T> payload) {
        sender.onNext(payload);
    }
}
