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
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * A rate limiting operator based off the token bucket algorithm. From
 * <a href="https://aliz.ai/rate-limiting-in-rxjs/">Rate Limiting in rxjs</a>.
 *
 * @param <T> the type of the transformed sequence
 */
public class RateLimitOperator<T> implements Function<Publisher<T>, Publisher<T>> {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    private final int id;
    private final Logger log;
    private final AtomicInteger tokens;
    private final Duration refillPeriod;
    private final Scheduler delayScheduler;
    private final ReplayProcessor<Integer> tokenChanged;
    private final FluxSink<Integer> tokenChangedSink;
    private final Scheduler tokenPublishScheduler;

    public RateLimitOperator(int capacity, Duration refillPeriod) {
        this(capacity, refillPeriod, Schedulers.parallel());
    }

    public RateLimitOperator(int capacity, Duration refillPeriod, Scheduler delayScheduler) {
        this.id = ID_GENERATOR.incrementAndGet();
        this.log = Loggers.getLogger("discord4j.limiter." + id);
        this.tokens = new AtomicInteger(capacity);
        this.refillPeriod = refillPeriod;
        this.delayScheduler = delayScheduler;
        this.tokenChanged = ReplayProcessor.cacheLastOrDefault(capacity);
        this.tokenChangedSink = tokenChanged.sink(FluxSink.OverflowStrategy.LATEST);
        this.tokenPublishScheduler = Schedulers.newSingle("d4j-limiter-" + id);
    }

    @Override
    public Publisher<T> apply(Publisher<T> source) {
        return Flux.from(source).flatMap(value -> availableTokens()
                .take(1)
                .log(log, Level.FINEST, false, SignalType.ON_SUBSCRIBE, SignalType.ON_NEXT)
                .map(token -> {
                    acquire();
                    Mono.delay(refillPeriod, delayScheduler).subscribe(__ -> release());
                    return value;
                }));
    }

    private void acquire() {
        int token = tokens.decrementAndGet();
        if (log.isTraceEnabled()) {
            log.trace("Acquired a token, {} tokens remaining", token);
        }
        tokenChangedSink.next(token);
    }

    private void release() {
        int token = tokens.incrementAndGet();
        if (log.isTraceEnabled()) {
            log.trace("Released a token, {} tokens remaining", token);
        }
        tokenChangedSink.next(token);
    }

    private Flux<Integer> availableTokens() {
        return tokenChanged.publishOn(tokenPublishScheduler).filter(__ -> tokens.get() > 0);
    }
}
