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

import discord4j.common.ReactorResources;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.response.ResponseFunction;

import java.util.List;
import java.util.Objects;

/**
 * Options used to control the behavior of a {@link Router}.
 */
public class RouterOptions {

    private final String token;
    private final ReactorResources reactorResources;
    private final ExchangeStrategies exchangeStrategies;
    private final /*~~>*/List<ResponseFunction> responseTransformers;
    private final GlobalRateLimiter globalRateLimiter;
    private final RequestQueueFactory requestQueueFactory;
    private final String discordBaseUrl;

    public RouterOptions(String token, ReactorResources reactorResources, ExchangeStrategies exchangeStrategies,
                         /*~~>*/List<ResponseFunction> responseTransformers, GlobalRateLimiter globalRateLimiter,
                         RequestQueueFactory requestQueueFactory, String discordBaseUrl) {
        this.token = Objects.requireNonNull(token, "token");
        this.reactorResources = Objects.requireNonNull(reactorResources, "reactorResources");
        this.exchangeStrategies = Objects.requireNonNull(exchangeStrategies, "exchangeStrategies");
        /*~~>*/this.responseTransformers = Objects.requireNonNull(responseTransformers, "responseTransformers");
        this.globalRateLimiter = Objects.requireNonNull(globalRateLimiter, "globalRateLimiter");
        this.requestQueueFactory = Objects.requireNonNull(requestQueueFactory, "requestQueueFactory");
        this.discordBaseUrl = Objects.requireNonNull(discordBaseUrl, "discordBaseUrl");
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
    public /*~~>*/List<ResponseFunction> getResponseTransformers() {
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

    /**
     * Returns the {@link RequestQueueFactory} to use for creating {@link RequestQueue} instances.
     *
     * @return the configured {@link RequestQueueFactory}
     */
    public RequestQueueFactory getRequestQueueFactory() {
        return requestQueueFactory;
    }

    /**
     * Returns the base url of the Discord API.
     *
     * @return the configured discord api base url
     */
    public String getDiscordBaseUrl() {
        return discordBaseUrl;
    }
}
