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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.rest.request;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

public class GlobalRateLimiterTest {

    @Test
    public void testGlobalRateLimiter() {
        GlobalRateLimiter rateLimiter = new GlobalRateLimiter();

        rateLimiter.rateLimitFor(Duration.ofSeconds(1));

        rateLimiter.onComplete().block();
        System.out.println("1");

        rateLimiter.rateLimitFor(Duration.ofSeconds(1));

        rateLimiter.onComplete().block();
        System.out.println("2");
    }

    @Test
    public void testBurstingRequestsGlobalRateLimiter() {
        GlobalRateLimiter rateLimiter = new GlobalRateLimiter();
        Random random = new Random();
        Flux.range(0, 100)
                .flatMap(index -> rateLimiter.withLimiter(() -> {
                    if (random.nextDouble() < 0.1) {
                        long delay = random.nextInt(500);
                        rateLimiter.rateLimitFor(Duration.ofMillis(delay));
                    }
                    return Mono.just(index);
                }))
                .blockLast();
    }
}
