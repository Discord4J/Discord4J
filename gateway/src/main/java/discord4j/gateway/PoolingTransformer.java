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

import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.pool.Pool;
import reactor.pool.PoolBuilder;
import reactor.pool.PooledRef;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class PoolingTransformer implements PayloadTransformer {

    private static final Logger log = Loggers.getLogger(PoolingTransformer.class);

    private final int capacity;
    private final long refillPeriodNanos;

    private final Pool<Permit> pool;
    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicLong nextRefillAt = new AtomicLong(0);

    public PoolingTransformer(int capacity, Duration refillPeriod) {
        this.capacity = capacity;
        this.refillPeriodNanos = refillPeriod.toNanos();
        this.pool = PoolBuilder.from(Mono.fromCallable(Permit::new))
                .sizeBetween(1, capacity)
                .releaseHandler(permit -> Mono.delay(getDurationUntilNextRefill().plus(permit.releaseDelay))
                        .doOnNext(tick -> log.warn("[{}] Released permit", permit))
                        .then())
                .fifo();
    }

    private Duration getDurationUntilNextRefill() {
        if (count.get() + 1 <= capacity) {
            return Duration.ZERO;
        }
        long now = System.nanoTime();
        return Duration.ofNanos(nextRefillAt.get() - now);
    }

    @Override
    public Publisher<ByteBuf> apply(Flux<Tuple2<GatewayClient, ByteBuf>> publisher) {
        AtomicReference<PooledRef<Permit>> emitted = new AtomicReference<>();
        return publisher.flatMap(t2 -> this.pool.acquire()
                .doOnNext(pooledRef -> {
                    log.warn("[{}] Acquired permit", pooledRef.poolable());
                    emitted.set(pooledRef);
                    long now = System.nanoTime();
                    if (nextRefillAt.get() <= now) {
                        count.set(0);
                        nextRefillAt.set(now + refillPeriodNanos);
                    }
                    if (count.get() + 1 <= capacity) {
                        count.incrementAndGet();
                    }
                    pooledRef.poolable().releaseDelay = Duration.ofMillis(t2.getT1().getResponseTime());
                })
                .doFinally(st -> {
                    PooledRef<Permit> ref = emitted.get();
                    if (ref != null && emitted.compareAndSet(ref, null)) {
                        log.warn("[{}] Releasing permit", ref.poolable());
                        ref.release().subscribe();
                    }
                })
                .thenReturn(t2.getT2()));
    }

    private static class Permit {
        private Duration releaseDelay = Duration.ZERO;
    }
}
