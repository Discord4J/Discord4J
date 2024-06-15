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
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.Semaphore;

/**
 * A {@link GlobalRateLimiter} that uses fair {@link Semaphore} instances as implementation. Can be configured with the
 * amount of permits it allows before blocking further acquire attempts until permits are released.
 */
public class SemaphoreGlobalRateLimiter implements GlobalRateLimiter {

    private final Semaphore outer;
    private final Semaphore inner = new Semaphore(1, true);
    private volatile long limitedUntil = 0;

    /**
     * Creates a new global rate limiter with the specified parallelism level.
     *
     * @param parallelism the maximum number of requests that this limiter will allow in parallel
     */
    public SemaphoreGlobalRateLimiter(int parallelism) {
        this.outer = new Semaphore(parallelism, true);
    }

    /**
     * Sets a new rate limit that will be applied to every new resource acquired.
     *
     * @param duration the {@link Duration} every new acquired resource should wait before being used
     */
    public Mono<Void> rateLimitFor(Duration duration) {
        return Mono.fromRunnable(() -> {
            limitedUntil = System.nanoTime() + duration.toNanos();
        });
    }

    /**
     * Returns a {@link Mono} indicating that the rate limit has ended.
     *
     * @return a {@link Mono} that completes when the currently set limit has completed
     */
    private Mono<Void> onComplete() {
        return Mono.defer(this::notifier);
    }

    private Mono<Void> notifier() {
        return getRemaining().flatMap(remaining -> {
            if (!remaining.isNegative() && !remaining.isZero()) {
                return Mono.delay(remaining).then();
            } else {
                return Mono.empty();
            }
        });
    }

    @Override
    public Mono<Duration> getRemaining() {
        return Mono.fromCallable(() -> Duration.ofNanos(limitedUntil - System.nanoTime()));
    }

    /**
     * Provides a scope to perform reactive operations under this limiter resources. Resources are acquired on
     * subscription and released when the given stage has completed or terminated with an error.
     *
     * @param stage a {@link Mono} that will manage this limiter resources
     * @param <T> the type of the stage supplier
     * @return a {@link Mono} where each subscription represents acquiring a rate limiter resource
     */
    public <T> Flux<T> withLimiter(Publisher<T> stage) {
        return Flux.usingWhen(
                acquire(),
                resource -> stage,
                this::release);
    }

    private Mono<Resource> acquire() {
        return Mono.defer(
                () -> {
                    outer.acquireUninterruptibly();
                    return getRemaining().map(remaining -> {
                        if (!remaining.isNegative() && !remaining.isZero()) {
                            inner.acquireUninterruptibly();
                            return new Resource(outer, inner);
                        }
                        return new Resource(outer, null);
                    });
                })
                .subscribeOn(Schedulers.boundedElastic())
                .delayUntil(resource -> onComplete());
    }

    private Mono<Void> release(Resource resource) {
        return Mono.fromRunnable(() -> {
            if (resource.inner != null) {
                resource.inner.release();
            }
            if (resource.outer != null) {
                resource.outer.release();
            }
        });
    }

    static class Resource {

        private final Semaphore outer;
        private final Semaphore inner;

        Resource(Semaphore outer, Semaphore inner) {
            this.outer = outer;
            this.inner = inner;
        }
    }
}
