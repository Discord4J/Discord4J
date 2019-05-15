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
public class PoolGlobalRateLimiter implements GlobalRateLimiter {

    private static final Logger log = Loggers.getLogger(PoolGlobalRateLimiter.class);

    private final Pool<Permit> outer;
    private volatile long limitedUntil = 0;

    /**
     * Creates a new global rate limiter with the specified parallelism level.
     *
     * @param parallelism the maximum number of requests that this limiter will allow in parallel
     */
    public PoolGlobalRateLimiter(int parallelism) {
        this.outer = PoolBuilder.from(Mono.fromCallable(this::newPermit))
                .sizeMax(parallelism)
                .releaseHandler(resource -> {
                    log.debug("[{}] Released permit", resource);
                    return Mono.empty();
                })
                .build();
    }

    private Permit newPermit() {
        return new Permit(Permit.ID.incrementAndGet());
    }

    @Override
    public void rateLimitFor(Duration duration) {
        limitedUntil = System.nanoTime() + duration.toNanos();
    }

    @Override
    public Duration getRemaining() {
        return Duration.ofNanos(limitedUntil - System.nanoTime());
    }

    @Override
    public <T> Flux<T> withLimiter(Publisher<T> stage) {
        return outer.withPoolable(permit -> Mono.subscriberContext()
                .flatMapMany(ctx -> {
                    permit.bucket = ctx.getOrDefault("bucket", "<unknown>");
                    log.debug("[{}] Acquired permit", permit);
                    Duration delay = getRemaining();
                    if (!delay.isNegative() && !delay.isZero()) {
                        log.debug("[{}] Waiting for {} before processing request", permit, delay);
                        return Mono.delay(delay).flatMapMany(tick -> Flux.from(stage));
                    } else {
                        return stage;
                    }
                }));
    }

    static class Permit {

        static final AtomicInteger ID = new AtomicInteger(0);

        final int id;
        String bucket;

        Permit(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return id + "-" + bucket;
        }
    }
}
