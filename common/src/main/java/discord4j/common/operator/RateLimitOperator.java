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

import discord4j.common.sinks.EmissionStrategy;
import org.reactivestreams.Publisher;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A rate limiting operator based off the token bucket algorithm. From
 * <a href="https://aliz.ai/rate-limiting-in-rxjs/">Rate Limiting in rxjs</a>.
 *
 * @param <T> the type of the transformed sequence
 */
public class RateLimitOperator<T> implements Function<Publisher<T>, Publisher<T>> {

    private static final Logger log = Loggers.getLogger("discord4j.limiter");
    private static final Supplier<Scheduler> DEFAULT_PUBLISH_SCHEDULER = () ->
            Schedulers.newSingle("d4j-limiter", true);

    private final AtomicInteger tokens;
    private final Duration refillPeriod;
    private final Scheduler delayScheduler;
    private final Sinks.Many<Integer> tokenSink;
    private final Scheduler tokenPublishScheduler;
    private final EmissionStrategy emissionStrategy;

    public RateLimitOperator(int capacity, Duration refillPeriod, Scheduler delayScheduler) {
        this(capacity, refillPeriod, delayScheduler, DEFAULT_PUBLISH_SCHEDULER.get());
    }

    public RateLimitOperator(int capacity, Duration refillPeriod, Scheduler delayScheduler, Scheduler publishScheduler) {
        this.tokens = new AtomicInteger(capacity);
        this.refillPeriod = refillPeriod;
        this.delayScheduler = delayScheduler;
        this.tokenSink = Sinks.many().replay().latestOrDefault(capacity);
        this.tokenPublishScheduler = publishScheduler;
        this.emissionStrategy = EmissionStrategy.park(Duration.ofNanos(10));
    }

    private String id() {
        return Integer.toHexString(hashCode());
    }

    @Override
    public Publisher<T> apply(Publisher<T> source) {
        return Flux.from(source).flatMap(value -> availableTokens()
                .next()
                .doOnSubscribe(s -> {
                    if (log.isTraceEnabled()) {
                        log.trace("[{}] Subscribed to limiter", id());
                    }
                })
                .map(token -> {
                    acquire();
                    Mono.delay(refillPeriod, delayScheduler).subscribe(__ -> release());
                    return value;
                }));
    }

    private void acquire() {
        int token = tokens.decrementAndGet();
        if (log.isTraceEnabled()) {
            log.trace("[{}] Acquired a token, {} tokens remaining", id(), token);
        }
        emissionStrategy.emitNext(tokenSink, token);
    }

    private void release() {
        int token = tokens.incrementAndGet();
        if (log.isTraceEnabled()) {
            log.trace("[{}] Released a token, {} tokens remaining", id(), token);
        }
        emissionStrategy.emitNext(tokenSink, token);
    }

    private Flux<Integer> availableTokens() {
        return tokenSink.asFlux().publishOn(tokenPublishScheduler).filter(__ -> tokens.get() > 0);
    }
}
