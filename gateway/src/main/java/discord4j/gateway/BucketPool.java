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

package discord4j.gateway;

import reactor.core.publisher.Mono;
import reactor.pool.Pool;
import reactor.pool.PoolBuilder;
import reactor.pool.PooledRef;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class BucketPool {

    private static final Logger log = Loggers.getLogger(BucketPool.class);

    private final int capacity;
    private final long refillPeriodNanos;

    private final Pool<Permit> pool;
    private final AtomicInteger permitId = new AtomicInteger(0);
    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicLong nextRefillAt = new AtomicLong(0);

    public BucketPool(int capacity, Duration refillPeriod) {
        this.capacity = capacity;
        this.refillPeriodNanos = refillPeriod.toNanos();
        this.pool = PoolBuilder.from(Mono.fromCallable(Permit::new))
                .sizeBetween(1, capacity)
                .releaseHandler(permit -> Mono.delay(getDurationUntilNextRefill().plus(permit.releaseDelay))
                        .doOnNext(tick -> log.trace(format("Released permit {}"), permit))
                        .then())
                .fifo();
    }

    public Mono<Void> acquire(Duration releaseDelay) {
        AtomicReference<PooledRef<Permit>> emitted = new AtomicReference<>();
        return pool.acquire()
                .doOnNext(pooledRef -> {
                    log.trace(format("Acquired permit {}"), pooledRef.poolable());
                    emitted.set(pooledRef);
                    long now = System.nanoTime();
                    if (nextRefillAt.get() <= now) {
                        count.set(0);
                        nextRefillAt.set(now + refillPeriodNanos);
                    }
                    if (count.get() + 1 <= capacity) {
                        count.incrementAndGet();
                    }
                    pooledRef.poolable().releaseDelay = releaseDelay;
                })
                .doFinally(st -> {
                    PooledRef<Permit> ref = emitted.get();
                    if (ref != null && emitted.compareAndSet(ref, null)) {
                        log.trace(format("Releasing permit {}"), ref.poolable());
                        ref.release().subscribe();
                    }
                })
                .then();
    }

    private Duration getDurationUntilNextRefill() {
        if (count.get() + 1 <= capacity) {
            return Duration.ZERO;
        }
        long now = System.nanoTime();
        return Duration.ofNanos(nextRefillAt.get() - now);
    }

    private String format(String message) {
        return "[pool-" + Integer.toHexString(hashCode()) + "] " + message;
    }

    private class Permit {

        private final int id;
        private Duration releaseDelay = Duration.ZERO;

        private Permit() {
            id = permitId.incrementAndGet();
        }

        @Override
        public String toString() {
            return "[id: " + id + ", 0x" + Integer.toHexString(hashCode()) + ']';
        }
    }
}
