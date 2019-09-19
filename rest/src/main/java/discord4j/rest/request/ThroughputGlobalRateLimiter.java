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

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>Custom implementation of {@link GlobalRateLimiter} that accepts a throughput
 * parameter, and adjusts the delay of requests in a such way that the effective
 * throughput complies with the targeted one.</p>
 *
 * <p>For example, if the parameter is set to 25, this limiter will allow a maximum
 * throughput of 25 requests per second.</p>
 *
 * <p>The effective throughput may be lower than the specified one if Discord's
 * global rate limit is being reached.</p>
 *
 * @author Alex1304
 */
public class ThroughputGlobalRateLimiter implements GlobalRateLimiter {

    private final long delayStepNanos;
    private final AtomicLong throughputLimitedUntil = new AtomicLong();
    private volatile long globallyRateLimitedUntil = 0;

    /**
     * Creates a new global rate limiter with the specified parallelism level.
     *
     * @param rps the maximum number of requests per second
     */
    public ThroughputGlobalRateLimiter(int rps) {
        if (rps < 1) {
            throw new IllegalArgumentException("rps must be >= 1");
        }
        this.delayStepNanos = 1_000_000_000 / rps;
    }

    @Override
    public Mono<Void> rateLimitFor(Duration duration) {
        return Mono.fromRunnable(() -> {
            globallyRateLimitedUntil = System.nanoTime() + duration.toNanos();
        });
    }

    @Override
    public Mono<Duration> getRemaining() {
        return Mono.just(Duration.ofNanos(globallyRateLimitedUntil - System.nanoTime()));
    }

    @Override
    public <T> Flux<T> withLimiter(Publisher<T> stage) {
        return Flux.defer(() -> {
            long now = System.nanoTime();
            long delay = throughputLimitedUntil.updateAndGet(current ->
                    Math.max(globallyRateLimitedUntil, Math.max(current + delayStepNanos, now)) - now);
            return Mono.delay(Duration.ofNanos(delay)).thenMany(stage);
        });
    }
}
