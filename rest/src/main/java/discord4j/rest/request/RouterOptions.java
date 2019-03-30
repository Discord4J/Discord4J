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

import discord4j.rest.response.ResponseFunction;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

/**
 * Options used to control the behavior of a {@link Router}.
 */
public class RouterOptions {

    private final Scheduler responseScheduler;
    private final Scheduler rateLimitScheduler;
    private final List<ResponseFunction> responseTransformers;

    protected RouterOptions(Builder builder) {
        this.responseScheduler = builder.responseScheduler;
        this.rateLimitScheduler = builder.rateLimitScheduler;
        this.responseTransformers = builder.responseTransformers;
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
     * Returns a new {@link RouterOptions} with default settings.
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

        private Scheduler responseScheduler = Schedulers.elastic();
        private Scheduler rateLimitScheduler = Schedulers.elastic();
        private final List<ResponseFunction> responseTransformers = new ArrayList<>();

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
         * all requests made by the {@code Router}.
         * <p>
         * The given {@link ResponseFunction} will be applied after every response. Calling
         * {@link #onClientResponse(ResponseFunction)} multiple times will result in additive behavior, so care must be
         * taken regarding the <strong>order</strong> in which multiple calls occur. Responses will processed in this
         * order and only the first matching one will be transformed.
         * <p>
         * Built-in factories are supplied for commonly used behavior:
         * <ul>
         * <li>{@link ResponseFunction}</li>
         * </ul>
         *
         * @param errorHandler
         * @return
         */
        public Builder onClientResponse(ResponseFunction errorHandler) {
            responseTransformers.add(errorHandler);
            return this;
        }

        public RouterOptions build() {
            return new RouterOptions(this);
        }
    }

    public Scheduler getResponseScheduler() {
        return responseScheduler;
    }

    public Scheduler getRateLimitScheduler() {
        return rateLimitScheduler;
    }

    public List<ResponseFunction> getResponseTransformers() {
        return responseTransformers;
    }
}
