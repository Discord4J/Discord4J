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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import discord4j.voice.json.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.NettyPipeline;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.Arrays;

import static discord4j.voice.VoiceGatewayClient.State.*;
import static io.netty.handler.codec.http.HttpHeaderNames.USER_AGENT;

public class VoiceGatewayClient extends FiniteStateMachine<VoiceGatewayClient.State, VoiceGatewayPayload<?>> {

    private final Logger log = Loggers.getLogger("discord4j.voice.gateway.client");
    private final FiniteStateMachine<State, VoiceGatewayPayload<?>> gatewayFSM;

    private final EmitterProcessor<VoiceGatewayPayload<?>> sender = EmitterProcessor.create(false);
    private final ObjectMapper mapper;
    private final VoiceSocket voiceSocket;

    public VoiceGatewayClient(long server_id, long user_id, String session_id, String token, ObjectMapper mapper) {
        this.mapper = mapper;
        this.voiceSocket = new VoiceSocket();
        this.gatewayFSM = new FiniteStateMachine<State, VoiceGatewayPayload<?>>() {{
            startWith(WAITING_FOR_HELLO);

            when(WAITING_FOR_HELLO)
                    .on(Hello.class, hello -> {
                        startHeartbeat((long) (hello.getData().heartbeat_interval * .75));
                        send(new Identify(Long.toUnsignedString(server_id), Long.toUnsignedString(user_id), session_id, token));
                        return WAITING_FOR_READY;
                    });

            when(WAITING_FOR_READY)
                    .on(Ready.class, ready -> {
                        voiceSocket.setup(ready.getData().ip, ready.getData().port)
                                .flatMap(ignored -> voiceSocket.performIpDiscovery(ready.getData().ssrc))
                                .subscribe(t -> {
                                    System.out.println("got here");
                                    String address = t.getT1();
                                    int port = t.getT2();
                                    send(new SelectProtocol("udp", address, port, VoiceSocket.ENCRYPTION_MODE));
                                });

                        return WAITING_FOR_SESSION_DESCRIPTION;
                    });

            when(WAITING_FOR_SESSION_DESCRIPTION)
                    .on(SessionDescription.class, sessionDesc -> {
                        voiceSocket.setSecretKey(sessionDesc.getData().secret_key);
                        return RECEIVING_EVENTS;
                    });

            whenAny().on(HeartbeatAck.class, ack -> {
                System.out.println("vGW got voice ack");
                return getCurrentState();
            });
        }};
    }

    public Mono<Void> execute(String gatewayUrl) {
        Mono<Void> httpFuture = HttpClient.create()
                .wiretap()
                .headers(headers -> headers.add(USER_AGENT, "DiscordBot(https://discord4j.com, 3)"))
                .websocket(Integer.MAX_VALUE)
                .uri(gatewayUrl + "?v=3")
                .handle(this::handle)
                .doOnComplete(() -> log.debug("Voice WebSocket future complete"))
                .doOnError(t -> log.debug("Voice WebSocket future threw an error", t))
                .doOnCancel(() -> log.debug("Voice WebSocket future cancelled"))
                .doOnTerminate(this::stopHeartbeat)
                .then();

        return httpFuture;
    }

    public Mono<Void> handle(WebsocketInbound in, WebsocketOutbound out) {
        Mono<Void> inboundThen = in.aggregateFrames()
                .receiveFrames()
                .map(WebSocketFrame::content)
                .map(buf -> {
                    try {
                        ByteBuf n = Unpooled.buffer(buf.readableBytes());
                        buf.readBytes(n);

                        return mapper.readTree(n.array());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null; // will kill everything
                    }
                })
                .doOnNext(this::receive)
                .doOnError(Throwable::printStackTrace)
                .doOnComplete(() -> System.out.println("wtf why"))
                .log("inbound meme")
                .then();

        Mono<Void> outboundThen = out.options(NettyPipeline.SendOptions::flushOnEach)
                .sendObject(sender.log("sender meme").map(payload -> {
                    try {
                        System.out.println(mapper.writeValueAsString(payload));
                        return new TextWebSocketFrame(Unpooled.wrappedBuffer(mapper.writeValueAsBytes(payload)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .doOnCancel(() -> System.out.println("This was cancelled")))
                .then();

        return Mono.zip(inboundThen, outboundThen).log("vc-handle-when").then();
//        return Mono.when(inboundThen, outboundThen).log("vc-handle-when");
    }

    public <T> void send(VoiceGatewayPayload<T> payload) {
        log.info("send ");
        sender.onNext(payload);
    }

    private Disposable heartbeat = null;
    private void startHeartbeat(long interval) {
        heartbeat = Flux.interval(Duration.ofMillis(interval)).subscribe(l -> send(new Heartbeat(l)));
    }

    private void stopHeartbeat() {
        heartbeat.dispose();
    }

    private void receive(JsonNode json) {
        log.debug(json.toString());
        int op = json.get("op").asInt();
        JsonNode d = json.get("d");

        try {
            switch (op) {
                case Hello.OP:
                    gatewayFSM.onEvent(new Hello(d.get("heartbeat_interval").asLong()));
                    break;
                case Ready.OP:
                    gatewayFSM.onEvent(new Ready(d.get("ssrc").asInt(), d.get("ip").asText(), d.get("port").asInt()));
                    break;
                case HeartbeatAck.OP:
                    gatewayFSM.onEvent(new HeartbeatAck(d.asLong()));
                    break;
                case SessionDescription.OP:
                    ArrayNode arrayNode = ((ArrayNode) d.get("secret_key"));
                    byte[] secret_key = mapper.readValue(arrayNode.traverse(mapper), byte[].class);

                    gatewayFSM.onEvent(new SessionDescription(d.get("mode").asText(), secret_key));
                    break;
                default:
                    System.out.println("unhandled: " + json.toString());
                    throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    enum State {
        WAITING_FOR_HELLO,
        WAITING_FOR_READY,
        WAITING_FOR_SESSION_DESCRIPTION,
        RECEIVING_EVENTS,
    }
}
