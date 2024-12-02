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
import reactor.core.scheduler.Scheduler;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * Custom implementation of {@link GlobalRateLimiter} that uses a clock ticking
 * at regular intervals in order to give permits for requests.
 * <p>
 * The effective throughput may be lower than the specified one if Discord's
 * global rate limit is being reached.
 *
 * @author Alex1304
 */
public class ClockGlobalRateLimiter implements GlobalRateLimiter {

    private final AtomicLong limitedUntil;
    private final AtomicInteger permitsRemaining;
    private final AtomicLong permitsResetAfter;

    /**
     * Creates a {@link ClockGlobalRateLimiter} with a specified interval and number of
     * permits per tick.
     *
     * @param permitsPerTick the max number of requests per tick
     * @param interval the interval between two ticks
     */
    public ClockGlobalRateLimiter(int permitsPerTick, Duration interval, Scheduler scheduler) {
        if ((Objects.requireNonNull(interval)).isNegative() || interval.isZero()) {
            throw new IllegalArgumentException("interval must be a non-zero positive duration");
        }
        this.limitedUntil = new AtomicLong();
        this.permitsRemaining = new AtomicInteger();
        this.permitsResetAfter = new AtomicLong();
        Flux.interval(interval, scheduler)
                .doOnNext(tick -> permitsRemaining.set(permitsPerTick))
                .doOnNext(tick -> permitsResetAfter.set(System.nanoTime() + interval.toNanos()))
                .subscribe();
    }

    @Override
    public Mono<Void> rateLimitFor(Duration duration) {
        return Mono.fromRunnable(() -> limitedUntil.set(System.nanoTime() + duration.toNanos()));
    }

    @Override
    public Mono<Duration> getRemaining() {
        return Mono.fromCallable(() -> Duration.ofNanos(limitedUntil.get() - System.nanoTime()));
    }

    @Override
    public <T> Flux<T> withLimiter(Publisher<T> stage) {
        AtomicLong retryIn = new AtomicLong();
        return Mono.create(
                sink -> {
                    retryIn.set(0);
                    long now = System.nanoTime();
                    if (permitsRemaining.decrementAndGet() < 0) {
                        retryIn.set(permitsResetAfter.get() - now);
                    }
                    if (now < limitedUntil.get()) {
                        retryIn.set(Math.max(retryIn.get(), limitedUntil.get() - now));
                    }
                    if (retryIn.get() > 0) {
                        sink.error(new RuntimeException());
                    } else {
                        sink.success();
                    }
                })
            .retryWhen(RetryBackoffSpec.backoff(1, Duration.ofNanos(retryIn.get())))
            .thenMany(stage);
    }
}
