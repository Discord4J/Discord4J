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

import java.time.Duration;

/**
 * Used to prevent requests from being sent while the bot is
 * <a href="https://discordapp.com/developers/docs/topics/rate-limits#exceeding-a-rate-limit">globally rate limited</a>.
 * <p>
 * Provides resources that can be acquired through the use of {@link #withLimiter(Publisher)}, and held until the
 * supplied stage completes or terminates with an error. If the limiter resources are exhausted, it will limit all
 * other attempts, waiting until a resource becomes available.
 * <p>
 * This rate limiter can have their delay directly modified through {@link #rateLimitFor(Duration)}, determining the
 * duration a resource holder must wait before processing starts.
 */
public interface GlobalRateLimiter {

    /**
     * Sets a new rate limit that will be applied to every operation performed using {@link #withLimiter(Publisher)}.
     *
     * @param duration the {@link Duration} every new operation should wait before being used
     */
    void rateLimitFor(Duration duration);

    /**
     * Returns the {@link Duration} remaining until the current global rate limit is completed. Can be negative or
     * zero if there is no currently active global rate limit.
     *
     * @return a positive {@link Duration} indicating the remaining time a global rate limit is being applied. Zero
     * or negative if no global rate limit is currently active.
     */
    Duration getRemaining();

    /**
     * Provides a scope to perform reactive operations under this global rate limiter. Limiter resources are acquired on
     * subscription and released when the given stage is cancelled, has completed or has been terminated with an error.
     *
     * @param stage a {@link Publisher} that will manage this global rate limiter resources
     * @param <T> the type of the stage supplier
     * @return a {@link Flux} where each subscription represents acquiring a rate limiter resource
     */
    <T> Flux<T> withLimiter(Publisher<T> stage);
}
