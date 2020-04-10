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

package discord4j.gateway.limiter;

import discord4j.common.operator.RateLimitOperator;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PayloadTransformerTest {

    private static final Logger log = Loggers.getLogger(PayloadTransformerTest.class);

    @Test
    @Ignore
    public void testIdentifySequence() {
        final int factor = 1;
        final Map<Integer, RateLimitOperator<String>> limiters = new ConcurrentHashMap<>(factor);
        final Map<Integer, AtomicLong> lastIdentify = new ConcurrentHashMap<>(factor);

        Flux<Integer> connections = Flux.range(0, factor * 8)
                .groupBy(shard -> shard % factor)
                .flatMap(group -> group.concatMap(index -> {
                    RateLimitOperator<String> limiter = limiters.computeIfAbsent(index % factor,
                            k -> new RateLimitOperator<>(1, Duration.ofSeconds(5), Schedulers.parallel()));
                    AtomicLong lastIdentifyAt = lastIdentify.computeIfAbsent(index % factor,
                            k -> new AtomicLong(0));

                    return Flux.just("identify: " + index)
                            .transform(limiter)
                            .doOnNext(it -> {
                                long now = System.nanoTime();
                                if (Duration.ofNanos(lastIdentifyAt.get()).plusSeconds(5).toNanos() > now) {
                                    log.warn("OP 9 !!! identified too quickly");
                                }
                                log.info(">> {}", it);
                                lastIdentifyAt.set(now);
                            })
                            .then()
                            .thenReturn(index);
                }));

        connections.blockLast();
    }

    @Test
    @Ignore
    public void testIdentifyLimiter() {
        RateLimitOperator<Integer> limiter = new RateLimitOperator<>(1, Duration.ofSeconds(5), Schedulers.parallel());

        Flux<Integer> outbound = Flux.range(0, 10)
                .transform(limiter)
                .doOnNext(value -> log.info(">> {}", value));

        outbound.blockLast();
    }

    @Test
    @Ignore
    public void testOutboundLimiter() {
        RateLimitOperator<Integer> limiter = new RateLimitOperator<>(120, Duration.ofMinutes(1), Schedulers.parallel());

        Flux<Integer> outbound = Flux.range(0, 200)
                .transform(limiter)
                .doOnNext(value -> log.info(">> {}", value));

        outbound.blockLast();
    }
}
