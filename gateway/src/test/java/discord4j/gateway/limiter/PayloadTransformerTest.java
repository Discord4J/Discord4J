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

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PayloadTransformerTest {

    private static final Logger log = Loggers.getLogger(PayloadTransformerTest.class);

    private final Map<Integer, BucketPoolTransformer<String>> limiters = new ConcurrentHashMap<>(1);

    @Test
    public void testIdentifySequence() {
        int factor = 1;

        Flux<Integer> connections = Flux.range(0, factor * 50)
                .groupBy(shard -> shard % factor)
                .flatMap(group -> group.concatMap(index -> acquire(index, getIdentifyLimiter(index, factor))));

        connections.blockLast();

    }

    public BucketPoolTransformer<String> getIdentifyLimiter(int index, int shardingFactor) {
        return limiters.computeIfAbsent(index % shardingFactor,
                k -> new BucketPoolTransformer<>(1, Duration.ofSeconds(6)));
    }

    private Mono<Integer> acquire(Integer index, BucketPoolTransformer<String> limiter) {
        return Mono.deferWithContext(ctx -> {
            log.info("{}", index);

            Flux<String> identify = Flux.just("identify")
                    .transform(seq -> limiter.apply(seq, () -> Duration.ZERO));

            return identify.doOnNext(it -> log.info(">> {}", it))
                    .flatMap(res -> Mono.delay(Duration.ofSeconds(5)))
                    .then()
                    .thenReturn(index);
        });
    }
}
