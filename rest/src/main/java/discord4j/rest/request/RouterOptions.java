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

import discord4j.common.annotations.Experimental;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Route;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Options used to control the behavior of a {@link Router}.
 */
public class RouterOptions {

    /**
     * The default {@link Scheduler} to publish responses. Allows blocking usage.
     */
    public static final Scheduler DEFAULT_RESPONSE_SCHEDULER = Schedulers.elastic();

    /**
     * The default {@link Scheduler} to delay rate limited requests.
     */
    public static final Scheduler DEFAULT_RATE_LIMIT_SCHEDULER = Schedulers.newParallel("discord4j-worker");

    /**
     * The default number of router requests allowed in parallel.
     */
    public static final int DEFAULT_REQUEST_PARALLELISM = 8;

    private final Scheduler responseScheduler;
    private final Scheduler rateLimitScheduler;
    private final List<ResponseFunction> responseTransformers;
    private final int requestParallelism;
    private final GlobalRateLimiter globalRateLimiter;

    protected RouterOptions(Builder builder) {
        this.responseScheduler = builder.responseScheduler;
        this.rateLimitScheduler = builder.rateLimitScheduler;
        this.responseTransformers = builder.responseTransformers;
        if (builder.globalRateLimiter != null) {
            this.requestParallelism = -1; // @deprecated
            this.globalRateLimiter = builder.globalRateLimiter;
        } else {
            this.requestParallelism = builder.requestParallelism;
            this.globalRateLimiter = new PoolGlobalRateLimiter(builder.requestParallelism);
        }
    }

    /**
     * Returns a new {@link RouterOptions.Builder} to construct {@link RouterOptions}.
     *
     * @return a new {@code RouterOptions} builder
     */
    public static RouterOptions.Builder builder() {
        return new RouterOptions.Builder();
    }

    /**
     * Returns a new {@link RouterOptions} with default settings. See {@link #DEFAULT_RESPONSE_SCHEDULER} and
     * {@link #DEFAULT_RATE_LIMIT_SCHEDULER} for the default values.
     *
     * @return a new {@code RouterOptions}
     */
    public static RouterOptions create() {
        return builder().build();
    }

    /**
     * Builder for {@link RouterOptions}.
     */
    public static class Builder {

        private Scheduler responseScheduler = DEFAULT_RESPONSE_SCHEDULER;
        private Scheduler rateLimitScheduler = DEFAULT_RATE_LIMIT_SCHEDULER;
        private final List<ResponseFunction> responseTransformers = new ArrayList<>();
        private int requestParallelism = DEFAULT_REQUEST_PARALLELISM;
        @Nullable
        private GlobalRateLimiter globalRateLimiter;

        protected Builder() {
        }

        /**
         * Sets the {@link Scheduler} used to process API responses. Defaults to {@link Schedulers#elastic()}.
         *
         * @param responseScheduler the {@code Scheduler} used to process responses
         * @return this builder
         */
        public Builder responseScheduler(Scheduler responseScheduler) {
            this.responseScheduler = responseScheduler;
            return this;
        }

        /**
         * Sets the {@link Scheduler} used to handle delays introduced by rate limiting. Defaults to
         * {@link Schedulers#elastic()}.
         *
         * @param rateLimitScheduler the {@code Scheduler} used to handle rate limiting
         * @return this builder
         */
        public Builder rateLimitScheduler(Scheduler rateLimitScheduler) {
            this.rateLimitScheduler = rateLimitScheduler;
            return this;
        }

        /**
         * Sets a new API response behavior to the supporting {@link Router}, allowing cross-cutting behavior across
         * all requests made by it.
         * <p>
         * The given {@link ResponseFunction} will be applied after every response. Calling this function multiple
         * times will result in additive behavior, so care must be taken regarding the <strong>order</strong> in
         * which multiple calls occur. Transformations will be added to the response pipeline in that order.
         * <p>
         * Built-in factories are supplied for commonly used behavior:
         * <ul>
         * <li>{@link ResponseFunction#emptyIfNotFound()} transforms any HTTP 404 error into an empty sequence.</li>
         * <li>{@link ResponseFunction#emptyIfNotFound(RouteMatcher)} transforms HTTP 404 errors from the given
         * {@link Route}s into an empty sequence.</li>
         * <li>{@link ResponseFunction#emptyOnErrorStatus(RouteMatcher, Integer...)} provides the same behavior as
         * above but for any given status codes.</li>
         * <li>{@link ResponseFunction#retryOnceOnErrorStatus(Integer...)} retries once for the given status codes.</li>
         * <li>{@link ResponseFunction#retryOnceOnErrorStatus(RouteMatcher, Integer...)} provides the same behavior
         * as above but for any matching {@link Route}.</li>
         * </ul>
         *
         * @param errorHandler the {@link ResponseFunction} to transform the responses from matching requests.
         * @return this builder
         */
        @Experimental
        public Builder onClientResponse(ResponseFunction errorHandler) {
            responseTransformers.add(errorHandler);
            return this;
        }

        /**
         * Define the level of parallel requests the configured {@link Router} should be allowed to make. In-flight
         * requests beyond the parallelism value will wait until a permit is released.
         * <p>
         * Modifying this value can increase the API request throughput at the cost of potentially hitting the global
         * rate limit. Defaults to {@link #DEFAULT_REQUEST_PARALLELISM}.
         *
         * @param requestParallelism the number of parallel requests allowed
         * @return this builder
         */
        public Builder requestParallelism(int requestParallelism) {
            this.requestParallelism = requestParallelism;
            return this;
        }

        /**
         * Define the {@link GlobalRateLimiter} to be applied while configuring the {@link Router} for a client.
         * {@link GlobalRateLimiter} purpose is to coordinate API requests to properly delay them under global rate
         * limiting scenarios. {@link RouterFactory} is responsible for applying the given implementation when building
         * the {@link Router}.
         * <p>
         * Setting a limiter here will override any value set on {@link #requestParallelism(int)}.
         *
         * @param globalRateLimiter the limiter instance to be used while configuring a {@link Router}, if supported by
         * the used {@link RouterFactory}
         * @return this builder
         * @see GlobalRateLimiter
         */
        public Builder globalRateLimiter(GlobalRateLimiter globalRateLimiter) {
            this.globalRateLimiter = globalRateLimiter;
            return this;
        }

        /**
         * Creates the {@link RouterOptions} object.
         *
         * @return the resulting {@link RouterOptions}
         */
        public RouterOptions build() {
            return new RouterOptions(this);
        }
    }

    /**
     * Returns the defined response scheduler. Allows flexibility for blocking usage if a {@link Scheduler} that allows
     * blocking is set.
     *
     * @return this option's response {@link Scheduler}
     */
    public Scheduler getResponseScheduler() {
        return responseScheduler;
    }

    /**
     * Returns the defined scheduler for rate limiting delay purposes.
     *
     * @return this option's rate limiting {@link Scheduler}
     */
    public Scheduler getRateLimitScheduler() {
        return rateLimitScheduler;
    }

    /**
     * Returns the list of {@link ResponseFunction} transformations that can be applied to every response. They are
     * to be
     * processed in the given order.
     *
     * @return a list of {@link ResponseFunction} objects.
     */
    public List<ResponseFunction> getResponseTransformers() {
        return responseTransformers;
    }

    /**
     * Returns the number of allowed parallel requests the configured {@link Router} should adhere to.
     *
     * @return the number of allowed parallel requests.
     * @deprecated for removal, using {@link #getGlobalRateLimiter()} instead
     */
    @Deprecated
    public int getRequestParallelism() {
        return requestParallelism;
    }

    /**
     * Returns the currently configured {@link GlobalRateLimiter}. Defaults to {@link SemaphoreGlobalRateLimiter} with
     * parallelism of {@link #DEFAULT_REQUEST_PARALLELISM} or the value supplied via the builder.
     *
     * @return the configured {@link GlobalRateLimiter}
     */
    public GlobalRateLimiter getGlobalRateLimiter() {
        return globalRateLimiter;
    }
}
