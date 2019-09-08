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

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GlobalRateLimiterTest {

    private static final Logger log = Loggers.getLogger(GlobalRateLimiterTest.class);

    private List<GlobalRateLimiter> limiters() {
        return Arrays.asList(
                new SemaphoreGlobalRateLimiter(8),
                new PoolGlobalRateLimiter(8));
    }

    @Test
    public void testGlobalRateLimiter() {
        for (GlobalRateLimiter rateLimiter : limiters()) {
            rateLimiter.rateLimitFor(Duration.ofSeconds(1));
            rateLimiter.withLimiter(Mono.just("1").doOnNext(log::info)).blockLast();
            rateLimiter.rateLimitFor(Duration.ofSeconds(1));
            rateLimiter.withLimiter(Mono.just("2").doOnNext(log::info)).blockLast();
        }
    }

    @Test
    public void testBurstingRequestsGlobalRateLimiter() {
        for (GlobalRateLimiter rateLimiter : limiters()) {
            Random random = new Random();
            Flux.range(0, 100)
                    .flatMap(index -> rateLimiter.withLimiter(Mono.defer(() -> {
                        if (random.nextDouble() < 0.1) {
                            long delay = random.nextInt(500);
                            rateLimiter.rateLimitFor(Duration.ofMillis(delay));
                        }
                        return Mono.just(index);
                    })))
                    .blockLast();
        }
    }
}
