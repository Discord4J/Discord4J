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

import discord4j.gateway.PayloadTransformer;
import discord4j.gateway.SessionInfo;
import discord4j.gateway.ShardInfo;
import discord4j.rest.request.RouterException;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class RSocketShardCoordinator implements ShardCoordinator {

    private static final Logger log = Loggers.getLogger(RSocketShardCoordinator.class);

    private final InetSocketAddress socketAddress;
    private final AtomicReference<Mono<RSocket>> socket = new AtomicReference<>();

    public RSocketShardCoordinator(InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    private Mono<RSocket> getSocket() {
        return socket.updateAndGet(rSocket -> rSocket != null ? rSocket : createSocket());
    }

    private Mono<RSocket> createSocket() {
        return RSocketFactory.connect()
                .errorConsumer(t -> log.error("Client error: {}", t.toString()))
                .transport(TcpClientTransport.create(socketAddress))
                .start()
                .doOnSubscribe(s -> log.debug("Connecting to RSocket server: {}", socketAddress))
                .cache(rSocket -> Duration.ofHours(1), t -> Duration.ZERO, () -> Duration.ZERO);
    }

    private <T> Flux<T> withSocket(Function<? super RSocket, Publisher<? extends T>> socketFunction) {
        return Mono.defer(this::getSocket)
                .flatMap(rSocket -> {
                    if (rSocket.isDisposed()) {
                        socket.set(null);
                        return Mono.error(new RouterException("Lost connection to leader"));
                    } else {
                        return Mono.just(rSocket);
                    }
                })
                .flatMapMany(socketFunction)
                .retryWhen(Retry.anyOf(RouterException.class)
                        .exponentialBackoffWithJitter(Duration.ofSeconds(2), Duration.ofMinutes(1))
                        .doOnRetry(rc -> {
                            socket.set(null);
                            log.info("Reconnecting to leader: {}", rc.exception().toString());
                        }));
    }

    @Override
    public Function<Flux<ShardInfo>, Flux<ShardInfo>> getConnectOperator() {
        // this client should only pick up
        String id = Integer.toHexString(hashCode());
        return sequence -> sequence.zipWith(
                withSocket(rSocket -> rSocket.requestStream(DefaultPayload.create("connect:" + id))
                        .onErrorMap(RouterException::new)
                        .doOnNext(payload -> log.debug(">: {}", payload.getDataUtf8()))
                        .filter(payload -> payload.getDataUtf8().contains(id))
                        .doOnNext(payload -> log.debug("Accepting connect notification"))), (shard, response) -> shard);
    }

    @Override
    public PayloadTransformer getIdentifyLimiter() {
        return sequence -> sequence.flatMap(t2 -> withSocket(rSocket ->
                rSocket.requestResponse(DefaultPayload.create("identify:" + t2.getT1().getResponseTime().toString()))
                        .onErrorMap(RouterException::new)
                        .doOnNext(payload -> log.debug(">: {}", payload.getDataUtf8())))
                .then(Mono.just(t2.getT2()))
        );
    }

    @Override
    public Mono<Void> publishConnected(ShardInfo shard) {
        return withSocket(rSocket -> rSocket.requestResponse(DefaultPayload.create("connect:" + (shard.getIndex() + 1))))
                .onErrorMap(RouterException::new)
                .doOnNext(payload -> log.debug(">: {}", payload.getDataUtf8()))
                .then();
    }

    @Override
    public Mono<Void> publishDisconnected(ShardInfo shard, SessionInfo session) {
        return withSocket(rSocket -> rSocket.requestResponse(DefaultPayload.create("disconnect:" + shard.getIndex())))
                .onErrorMap(RouterException::new)
                .doOnNext(payload -> log.debug(">: {}", payload.getDataUtf8()))
                .then();
    }
}
