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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class GlobalRateLimiterTest {

    private static final Logger log = Loggers.getLogger(GlobalRateLimiterTest.class);

    private List<GlobalRateLimiter> limiters() {
        return Arrays.asList(
                new ClockGlobalRateLimiter(50, Duration.ofSeconds(1), Schedulers.parallel()),
                new SemaphoreGlobalRateLimiter(16),
                new UnboundedGlobalRateLimiter()
                );
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
}
