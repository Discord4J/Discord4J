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

/**
 * A simplified {@link GlobalRateLimiter} implementation that accepts all requests and only introduces a delay if a
 * global limit is active.
 */
public class UnboundedGlobalRateLimiter implements GlobalRateLimiter {

    private volatile long limitedUntil = 0;

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
        return getRemaining()
                .filter(delay -> delay.getSeconds() > 0)
                .flatMapMany(delay -> Mono.delay(delay).flatMapMany(tick -> Flux.from(stage)))
                .switchIfEmpty(stage);
    }
}
