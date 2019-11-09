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

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.pool.Pool;
import reactor.pool.PoolBuilder;

import java.time.Duration;

/**
 * An implementation of {@link GlobalRateLimiter} that accepts all requests until a global limit exists, in which case
 * further requests go through a single-resource pool that will queue future requests until the limit expires.
 */
public class PooledGlobalRateLimiter implements GlobalRateLimiter {

    private final Pool<Object> pool;

    private volatile long limitedUntil = 0;

    public PooledGlobalRateLimiter() {
        this.pool = PoolBuilder.from(Mono.just(new Object()))
                .sizeBetween(1, 1)
                .fifo();
    }

    @Override
    public Mono<Void> rateLimitFor(Duration duration) {
        return Mono.fromRunnable(() -> limitedUntil = System.nanoTime() + duration.toNanos());
    }

    @Override
    public Mono<Duration> getRemaining() {
        return Mono.fromCallable(() -> Duration.ofNanos(limitedUntil - System.nanoTime()));
    }

    @Override
    public <T> Flux<T> withLimiter(Publisher<T> stage) {
        return getRemaining()
                .filter(delay -> delay.getSeconds() > 0)
                .flatMapMany(__ -> pool.withPoolable(___ -> getRemaining()
                        .flatMap(Mono::delay)
                        .flatMapMany(tick -> Flux.from(stage))))
                .switchIfEmpty(stage);
    }
}
