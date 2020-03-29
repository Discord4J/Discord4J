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
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An implementation of {@link GlobalRateLimiter} that uses a reactive pool of objects to coordinate requests. Can be
 * configured with the amount of resources the pool should allow before delaying all further acquire attempts until
 * resources become available.
 */
public class ParallelGlobalRateLimiter implements GlobalRateLimiter {

    private static final Logger log = Loggers.getLogger(ParallelGlobalRateLimiter.class);

    private final Pool<Permit> outer;
    private final AtomicInteger permitId = new AtomicInteger(0);

    private volatile long limitedUntil = 0;

    /**
     * Creates a new global rate limiter with the specified parallelism level.
     *
     * @param parallelism the maximum number of requests that this limiter will allow in parallel
     */
    public ParallelGlobalRateLimiter(int parallelism) {
        this.outer = PoolBuilder.from(Mono.fromCallable(this::newPermit))
                .sizeBetween(1, parallelism)
                .releaseHandler(resource -> {
                    log.trace(format("Released permit {}"), resource);
                    return Mono.empty();
                })
                .fifo();
    }

    private Permit newPermit() {
        return new Permit();
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
        return outer.withPoolable(permit -> {
            log.trace(format("Acquired permit {}"), permit);
            return getRemaining()
                    .filter(delay -> delay.getSeconds() > 0)
                    .flatMapMany(delay -> {
                        log.trace(format("Delay permit {} for {}"), permit, delay);
                        return Mono.delay(delay).flatMapMany(tick -> Flux.from(stage));
                    })
                    .switchIfEmpty(stage);
        });
    }

    private String format(String message) {
        return "[pool-" + Integer.toHexString(hashCode()) + "] " + message;
    }

    private class Permit {

        private final int id;

        private Permit() {
            id = permitId.incrementAndGet();
        }

        @Override
        public String toString() {
            return "[id: " + id + ", 0x" + Integer.toHexString(hashCode()) + ']';
        }
    }
}
