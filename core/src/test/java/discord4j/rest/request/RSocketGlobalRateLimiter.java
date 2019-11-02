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

package discord4j.rest.request;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.retry.Retry;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class RSocketGlobalRateLimiter implements GlobalRateLimiter {

    private static final Logger log = Loggers.getLogger(RSocketGlobalRateLimiter.class);
    private static final String ACQUIRE = "ACQUIRE";
    private static final String PERMIT = "PERMIT";
    private static final String RELEASE = "RELEASE";
    private static final String LIMIT_GLOBAL = "LIMIT:global";
    private static final String LIMIT_QUERY = "QUERY:global";

    private final InetSocketAddress socketAddress;
    private final AtomicReference<Mono<RSocket>> socket = new AtomicReference<>();

    public RSocketGlobalRateLimiter(InetSocketAddress socketAddress) {
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
                        return Mono.error(new RouterException("Lost connection to server"));
                    } else {
                        return Mono.just(rSocket);
                    }
                })
                .flatMapMany(socketFunction)
                .retryWhen(Retry.anyOf(RouterException.class)
                        .exponentialBackoffWithJitter(Duration.ofSeconds(2), Duration.ofMinutes(1))
                        .doOnRetry(rc -> {
                            socket.set(null);
                            log.info("Reconnecting to server: {}", rc.exception().toString());
                        }));
    }

    @Override
    public Mono<Void> rateLimitFor(Duration duration) {
        return withSocket(rSocket -> rSocket.requestResponse(limitPayload(duration))).then();
    }

    @Override
    public Mono<Duration> getRemaining() {
        return withSocket(rSocket -> rSocket.requestResponse(queryLimit())
                .map(payload -> {
                    String content = payload.getDataUtf8();
                    if (content.startsWith(LIMIT_QUERY)) {
                        String[] tokens = content.split(":", 4);
                        Duration remaining = Duration.ofNanos(Long.parseLong(tokens[2]));
                        long at = Long.parseLong(tokens[3]);
                        Duration lag = Duration.ofNanos(System.nanoTime() - at);
                        log.debug("Remaining global limit: {} (delta: {})", remaining, lag);
                        return orZero(remaining.minus(lag));
                    } else {
                        log.warn("Unknown payload: {}", content);
                    }
                    return Duration.ZERO;
                }))
                .next();
    }

    private static Duration orZero(Duration duration) {
        return duration.isNegative() ? Duration.ZERO : duration;
    }

    @Override
    public <T> Flux<T> withLimiter(Publisher<T> stage) {
        return withSocket(rSocket -> {
            UnicastProcessor<Payload> toLeader = UnicastProcessor.create();
            String id = Integer.toHexString(System.identityHashCode(stage));
            toLeader.onNext(acquirePayload(id));
            return rSocket.requestChannel(toLeader)
                    .onErrorMap(RouterException::new)
                    .doOnSubscribe(s -> log.info("[{}] Subscribed to RSocketGRL pipeline", id))
                    .doFinally(s -> log.info("[{}] Released RSocketGRL pipeline: {}", id, s))
                    .flatMap(payload -> {
                        String content = payload.getDataUtf8();
                        if (content.startsWith(PERMIT)) {
                            return Flux.from(stage)
                                    .doOnTerminate(() -> {
                                        log.debug("[{}] Request completed", id);
                                        toLeader.onNext(releasePayload(id));
                                        toLeader.onComplete();
                                    });
                        } else {
                            log.warn("Unknown payload: {}", content);
                        }
                        return Mono.empty();
                    });
        });
    }

    private static Payload limitPayload(Duration duration) {
        return DefaultPayload.create(LIMIT_GLOBAL + ":" + duration.toNanos() + ":" + System.nanoTime());
    }

    private static Payload queryLimit() {
        return DefaultPayload.create(LIMIT_QUERY);
    }

    private static Payload acquirePayload(String id) {
        return DefaultPayload.create(ACQUIRE + ":" + id);
    }

    private static Payload releasePayload(String id) {
        return DefaultPayload.create(RELEASE + ":" + id);
    }
}
