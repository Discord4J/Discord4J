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

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.ReactorResources;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Route;
import reactor.netty.http.client.HttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Options used to control the behavior of a {@link Router}.
 */
public class RouterOptions {

    /**
     * The default number of router requests allowed in parallel.
     */
    public static final int DEFAULT_REQUEST_PARALLELISM = 12;

    private final String token;
    private final ReactorResources reactorResources;
    private final ExchangeStrategies exchangeStrategies;
    private final List<ResponseFunction> responseTransformers;
    private final GlobalRateLimiter globalRateLimiter;

    protected RouterOptions(Builder builder) {
        this.token = Objects.requireNonNull(builder.token, "token");
        this.reactorResources = Objects.requireNonNull(builder.reactorResources, "reactorResources");
        this.exchangeStrategies = Objects.requireNonNull(builder.exchangeStrategies, "exchangeStrategies");
        this.responseTransformers = Objects.requireNonNull(builder.responseTransformers, "responseTransformers");
        if (builder.globalRateLimiter != null) {
            this.globalRateLimiter = builder.globalRateLimiter;
        } else {
            this.globalRateLimiter = new PoolGlobalRateLimiter(DEFAULT_REQUEST_PARALLELISM);
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
     * Create a {@link Builder} using the options configured in this instance.
     *
     * @return a new builder using the current options
     */
    public Builder mutate() {
        Builder builder = new Builder();

        builder.setToken(getToken())
                .setReactorResources(getReactorResources())
                .setExchangeStrategies(getExchangeStrategies())
                .setGlobalRateLimiter(getGlobalRateLimiter());
        getResponseTransformers().forEach(builder::onClientResponse);
        return builder;
    }

    /**
     * Builder for {@link RouterOptions}.
     */
    public static class Builder {

        private String token;
        private ReactorResources reactorResources;
        private ExchangeStrategies exchangeStrategies;
        private final List<ResponseFunction> responseTransformers = new ArrayList<>();
        private GlobalRateLimiter globalRateLimiter;

        protected Builder() {
        }

        /**
         * Set the token to authenticate a {@link Router} to the Discord REST API.
         *
         * @param token the bot authentication token
         * @return this builder
         */
        public Builder setToken(String token) {
            this.token = Objects.requireNonNull(token, "token");
            return this;
        }

        /**
         * Set a new {@link ReactorResources} dedicated to set up a connection pool, an event pool, as well as the
         * supporting {@link HttpClient} used for making rest requests and maintaining gateway connections.
         *
         * @param reactorResources the new resource provider used for rest and gateway operations
         * @return this builder
         */
        public Builder setReactorResources(ReactorResources reactorResources) {
            this.reactorResources = Objects.requireNonNull(reactorResources, "reactorResources");
            return this;
        }

        /**
         * Set the strategies to use when reading or writing HTTP request and response body entities.
         *
         * @param exchangeStrategies the HTTP exchange strategies to use
         * @return this builder
         * @see ExchangeStrategies#jackson(ObjectMapper)
         */
        public Builder setExchangeStrategies(ExchangeStrategies exchangeStrategies) {
            this.exchangeStrategies = Objects.requireNonNull(exchangeStrategies, "exchangeStrategies");
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
        public Builder onClientResponse(ResponseFunction errorHandler) {
            responseTransformers.add(Objects.requireNonNull(errorHandler, "errorHandler"));
            return this;
        }

        /**
         * Define the {@link GlobalRateLimiter} to be applied while configuring the {@link Router} for a client.
         * {@link GlobalRateLimiter} purpose is to coordinate API requests to properly delay them under global rate
         * limiting scenarios.
         *
         * @param globalRateLimiter the limiter instance to be used while configuring a {@link Router}
         * @return this builder
         * @see GlobalRateLimiter
         */
        public Builder setGlobalRateLimiter(GlobalRateLimiter globalRateLimiter) {
            this.globalRateLimiter = Objects.requireNonNull(globalRateLimiter, "globalRateLimiter");
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
     * Returns the currently configured token.
     *
     * @return the configured token
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns the currently configured {@link ReactorResources}.
     *
     * @return the configured {@link ReactorResources}
     */
    public ReactorResources getReactorResources() {
        return reactorResources;
    }

    /**
     * Returns the currently configured {@link ExchangeStrategies}.
     *
     * @return the configured {@link ExchangeStrategies}
     */
    public ExchangeStrategies getExchangeStrategies() {
        return exchangeStrategies;
    }

    /**
     * Returns the list of {@link ResponseFunction} transformations that can be applied to every response. They are
     * to be processed in the given order.
     *
     * @return a list of {@link ResponseFunction} objects.
     */
    public List<ResponseFunction> getResponseTransformers() {
        return responseTransformers;
    }

    /**
     * Returns the currently configured {@link GlobalRateLimiter}.
     *
     * @return the configured {@link GlobalRateLimiter}
     */
    public GlobalRateLimiter getGlobalRateLimiter() {
        return globalRateLimiter;
    }
}
