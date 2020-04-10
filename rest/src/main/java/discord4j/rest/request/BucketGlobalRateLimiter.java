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

import discord4j.common.operator.RateLimitOperator;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;

/**
 * An implementation of {@link GlobalRateLimiter} that uses a {@link RateLimitOperator} coordinate requests, injecting
 * an additional delay if a source trips the limiter early through {@link #rateLimitFor(Duration)}.
 */
public class BucketGlobalRateLimiter implements GlobalRateLimiter {

    private static final Logger log = Loggers.getLogger(BucketGlobalRateLimiter.class);

    private final RateLimitOperator<Integer> operator;

    private volatile long limitedUntil = 0;

    BucketGlobalRateLimiter(int capacity, Duration refillPeriod, Scheduler delayScheduler) {
        this.operator = new RateLimitOperator<>(capacity, refillPeriod, delayScheduler);
    }

    /**
     * Creates a new global rate limiter of 50 requests per second using {@link Schedulers#parallel()} to inject delays.
     *
     * @return a {@link BucketGlobalRateLimiter} with default parameters
     */
    public static BucketGlobalRateLimiter create() {
        return new BucketGlobalRateLimiter(50, Duration.ofSeconds(1), Schedulers.parallel());
    }

    /**
     * Creates a new global rate limiter with the given parameters. Be aware that modifying these parameters can lead
     * your bot hitting 429 TOO MANY REQUESTS errors.
     *
     * @param capacity the number of requests that can be performed in the given {@code refillPeriod}
     * @param refillPeriod the {@link Duration} before refilling request permits
     * @param delayScheduler the {@link Scheduler} used to inject delays
     * @return a {@link BucketGlobalRateLimiter} with the given parameters.
     */
    public static BucketGlobalRateLimiter create(int capacity, Duration refillPeriod, Scheduler delayScheduler) {
        return new BucketGlobalRateLimiter(capacity, refillPeriod, delayScheduler);
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
        return Mono.just(0)
                .transform(operator)
                .then(getRemaining())
                .filter(delay -> delay.getSeconds() > 0)
                .flatMapMany(delay -> {
                    log.trace("[{}] Delaying for {}", Integer.toHexString(hashCode()), delay);
                    return Mono.delay(delay).flatMapMany(tick -> Flux.from(stage));
                })
                .switchIfEmpty(stage);
    }
}
