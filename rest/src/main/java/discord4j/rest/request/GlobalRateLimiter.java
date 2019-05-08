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

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Used to prevent requests from being sent while the bot is
 * <a href="https://discordapp.com/developers/docs/topics/rate-limits#exceeding-a-rate-limit">globally rate limited</a>.
 * <p>
 * Provides a single resource that can be acquired through the use of {@link #withLimiter(Supplier)}, blocking all other
 * attempts until the {@link Mono} supplier completes or terminates with an error.
 * <p>
 * This rate limiter can have their delay directly indicated through {@link #rateLimitFor(Duration)}, determining the
 * duration a resource holder must wait before processing starts.
 * <p>
 * When subscribing to the rate limiter, the only guarantee is that the subscription will be completed at some point in
 * the future. If a global ratelimit is in effect, it will be completed when the cooldown ends. Otherwise, it is
 * completed immediately.
 */
public class GlobalRateLimiter {

    private final Semaphore outer;
    private final Semaphore inner = new Semaphore(1, true);
    private final AtomicLong limitedUntil = new AtomicLong(0L);

    /**
     * Creates a new global rate limiter with the specified parallelism level.
     *
     * @param parallelism the maximum number of requests that this limiter will allow in parallel
     */
    public GlobalRateLimiter(int parallelism) {
        this.outer = new Semaphore(parallelism, true);
    }

    /**
     * Sets a new rate limit that will be applied to every new resource acquired.
     *
     * @param duration the {@link Duration} every new acquired resource should wait before being used
     */
    public void rateLimitFor(Duration duration) {
        limitedUntil.set(System.nanoTime() + duration.toNanos());
    }

    /**
     * Returns a {@link Mono} indicating that the rate limit has ended.
     *
     * @return a {@link Mono} that completes when the currently set limit has completed
     */
    Mono<Void> onComplete() {
        return Mono.defer(this::notifier);
    }

    private Mono<Void> notifier() {
        long delayNanos = delayNanos();
        if (delayNanos > 0) {
            return Mono.delay(Duration.ofNanos(delayNanos), Schedulers.elastic()).then();
        }
        return Mono.empty();
    }

    long delayNanos() {
        return limitedUntil.get() - System.nanoTime();
    }

    /**
     * Provides a scope to perform reactive operations under this limiter resources. Resources are acquired on
     * subscription and released when the given stage has completed or terminated with an error.
     *
     * @param stage a supplier containing a {@link Mono} that will manage this limiter resources
     * @param <T> the type of the stage supplier
     * @return a {@link Mono} where each subscription represents acquiring a rate limiter resource
     */
    public <T> Mono<T> withLimiter(Supplier<Mono<T>> stage) {
        return Mono.usingWhen(
                acquire(),
                resource -> stage.get(),
                this::release,
                this::release);
    }

    private Mono<Resource> acquire() {
        return Mono
                .fromCallable(() -> {
                    outer.acquireUninterruptibly();
                    if (delayNanos() > 0) {
                        inner.acquireUninterruptibly();
                        return new Resource(outer, inner);
                    }
                    return new Resource(outer, null);
                })
                .subscribeOn(Schedulers.elastic())
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
