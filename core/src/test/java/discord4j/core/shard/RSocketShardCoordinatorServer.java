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
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class RSocketShardCoordinatorServer {

    private static final Logger log = Loggers.getLogger(RSocketShardCoordinatorServer.class);

    private final TcpServerTransport serverTransport;

    public RSocketShardCoordinatorServer(InetSocketAddress socketAddress) {
        this.serverTransport = TcpServerTransport.create(socketAddress);
    }

    public Mono<CloseableChannel> start() {
        EmitterProcessor<String> connect = EmitterProcessor.create(false);
        FluxSink<String> connectSink = connect.sink(FluxSink.OverflowStrategy.DROP);
        AtomicInteger clients = new AtomicInteger();
        BucketPool pool = new BucketPool(1, Duration.ofMillis(5500));
        return RSocketFactory.receive()
                .errorConsumer(t -> log.error("Server error: {}", t.toString()))
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
                .transport(serverTransport)
                .start();
    }
}
