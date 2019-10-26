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

package discord4j.core.shard;

import discord4j.gateway.BucketPool;
import discord4j.gateway.PayloadTransformer;
import discord4j.gateway.SessionInfo;
import discord4j.gateway.ShardInfo;
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class RSocketShardCoordinator implements ShardCoordinator {

    private static final Logger log = Loggers.getLogger(RSocketShardCoordinator.class);

    private final Mono<RSocket> clientSetup;

    public RSocketShardCoordinator(String address, int port) {
        this.clientSetup = RSocketFactory.connect()
                .transport(TcpClientTransport.create(address, port))
                .start()
                .cache();
    }

    public static Mono<CloseableChannel> startLocalServer(int port) {
        EmitterProcessor<String> connect = EmitterProcessor.create(false);
        FluxSink<String> connectSink = connect.sink(FluxSink.OverflowStrategy.DROP);
        AtomicInteger clients = new AtomicInteger();
        BucketPool pool = new BucketPool(1, Duration.ofSeconds(5));
        return RSocketFactory.receive()
                .acceptor((setup, sendingSocket) -> Mono.just(new AbstractRSocket() {

                    @Override
                    public Mono<Payload> requestResponse(Payload payload) {
                        // acquire for identify
                        String value = payload.getDataUtf8();
                        log.debug("Server received: {}", value);
                        if (value.startsWith("identify")) {
                            return pool.acquire(Duration.parse(value.split(":")[1]))
                                    .thenReturn(DefaultPayload.create("identify.success"));
                        } else if (value.startsWith("connect")) {
                            // notify server that a shard has connected
                            String metadata = value.split(":")[1];
                            connectSink.next("connect.success:" + metadata);
                            return Mono.just(DefaultPayload.create("connect.ack"));
                        } else if (value.startsWith("disconnect")) {
                            return Mono.just(DefaultPayload.create("disconnect.ack"));
                        } else {
                            return Mono.error(new RuntimeException("Unknown request"));
                        }
                    }

                    @Override
                    public Flux<Payload> requestStream(Payload payload) {
                        // acquire for connect
                        String value = payload.getDataUtf8();
                        if (value.startsWith("connect")) {
                            String metadata = value.split(":")[1];
                            if (clients.compareAndSet(0, 1)) {
                                log.debug("First connect stream client");
                                connectSink.next("connect.success:0:" + metadata);
                            } else {
                                log.debug("Connect stream clients: {}", clients.incrementAndGet());
                            }
                            return connect.map(str ->
                                    DefaultPayload.create(str + ":" + (str.contains(":0:") ? "" : metadata)))
                                    .doOnTerminate(clients::decrementAndGet);
                        } else {
                            return Flux.error(new RuntimeException("Unknown request"));
                        }
                    }
                }))
                .transport(TcpServerTransport.create("localhost", port))
                .start();
    }

    @Override
    public Function<Flux<ShardInfo>, Flux<ShardInfo>> getConnectOperator() {
        // this client should only pick up
        String id = Integer.toHexString(hashCode());
        return sequence -> sequence.zipWith(clientSetup.flatMapMany(
                rSocket -> rSocket.requestStream(DefaultPayload.create("connect:" + id))
                        .doOnNext(payload -> log.debug(">: {}", payload.getDataUtf8()))
                        .filter(payload -> payload.getDataUtf8().contains(id))
                        .doOnNext(payload -> log.debug("Accepting connect notification"))),
                (shard, response) -> shard);
    }

    @Override
    public PayloadTransformer getIdentifyLimiter() {
        return sequence -> sequence.flatMap(t2 -> clientSetup.flatMap(
                rSocket -> rSocket.requestResponse(DefaultPayload.create("identify:" + t2.getT1().getResponseTime().toString()))
                        .doOnNext(payload -> log.debug(">: {}", payload.getDataUtf8())))
                .thenReturn(t2.getT2()));
    }

    @Override
    public Mono<Void> publishConnected(ShardInfo shard) {
        return clientSetup.flatMap(rSocket -> rSocket.requestResponse(
                DefaultPayload.create("connect:" + (shard.getIndex() + 1))))
                .doOnNext(payload -> log.debug(">: {}", payload.getDataUtf8()))
                .then();
    }

    @Override
    public Mono<Void> publishDisconnected(ShardInfo shard, SessionInfo session) {
        return clientSetup.flatMap(rSocket -> rSocket.requestResponse(
                DefaultPayload.create("disconnect:" + shard.getIndex())))
                .doOnNext(payload -> log.debug(">: {}", payload.getDataUtf8()))
                .then();
    }
}
