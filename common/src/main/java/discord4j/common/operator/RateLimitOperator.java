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

package discord4j.common.operator;

import org.reactivestreams.Publisher;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * A rate limiting operator based off the token bucket algorithm. From
 * <a href="https://aliz.ai/rate-limiting-in-rxjs/">Rate Limiting in rxjs</a>.
 *
 * @param <T> the type of the transformed sequence
 */
public class RateLimitOperator<T> implements Function<Publisher<T>, Publisher<T>> {

    private final AtomicInteger tokens;
    private final Duration refillPeriod;
    private final Scheduler delayScheduler;
    private final EmitterProcessor<Integer> tokenChanged;

    public RateLimitOperator(int capacity, Duration refillPeriod) {
        this(capacity, refillPeriod, Schedulers.parallel());
    }

    public RateLimitOperator(int capacity, Duration refillPeriod, Scheduler delayScheduler) {
        this.tokens = new AtomicInteger(capacity);
        this.refillPeriod = refillPeriod;
        this.delayScheduler = delayScheduler;
        this.tokenChanged = EmitterProcessor.create(false);
        tokenChanged.onNext(tokens.get());
    }

    @Override
    public Publisher<T> apply(Publisher<T> source) {
        if (source instanceof Mono) {
            return Mono.from(source).flatMapMany(value -> availableTokens()
                    .take(1)
                    .map(token -> {
                        acquire();
                        Mono.delay(refillPeriod, delayScheduler).subscribe(__ -> release());
                        return value;
                    }));
        } else if (source instanceof Flux) {
            return Flux.from(source).flatMap(value -> availableTokens()
                    .take(1)
                    .map(token -> {
                        acquire();
                        Mono.delay(refillPeriod, delayScheduler).subscribe(__ -> release());
                        return value;
                    }));
        } else {
            throw new IllegalArgumentException("Unsupported publisher: " + source.getClass());
        }
    }

    private void acquire() {
        tokenChanged.onNext(tokens.decrementAndGet());
    }

    private void release() {
        tokenChanged.onNext(tokens.incrementAndGet());
    }

    private Flux<Integer> availableTokens() {
        return tokenChanged.filter(__ -> tokens.get() > 0);
    }
}
