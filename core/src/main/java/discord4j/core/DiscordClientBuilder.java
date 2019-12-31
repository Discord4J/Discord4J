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
package discord4j.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.GitProperties;
import discord4j.common.JacksonResources;
import discord4j.common.ReactorResources;
import discord4j.common.jackson.UnknownPropertyHandler;
import discord4j.rest.RestClient;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.request.*;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Route;
import reactor.core.publisher.Hooks;
import reactor.netty.http.client.HttpClient;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;

/**
 * Builder suited for creating a {@link DiscordClient}. To acquire an instance, see {@link #create(String)}.
 */
public final class DiscordClientBuilder<O extends RouterOptions> {

    private static final Logger log = Loggers.getLogger(DiscordClientBuilder.class);

    private final Function<RouterOptions, O> optionsModifier;

    private String token;
    private ReactorResources reactorResources;
    private JacksonResources jacksonResources;
    private ExchangeStrategies exchangeStrategies;
    private List<ResponseFunction> responseTransformers = new ArrayList<>();
    private GlobalRateLimiter globalRateLimiter;
    private boolean debugMode = true;

    /**
     * Initialize a new builder with the given token.
     *
     * @param token the bot token used to authenticate to Discord
     */
    public static DiscordClientBuilder<RouterOptions> create(String token) {
        return new DiscordClientBuilder<>(token, Function.identity());
    }

    DiscordClientBuilder(String token, Function<RouterOptions, O> optionsModifier) {
        this.token = Objects.requireNonNull(token, "token");
        this.optionsModifier = Objects.requireNonNull(optionsModifier, "optionsModifier");
    }

    DiscordClientBuilder(DiscordClientBuilder<?> source, Function<RouterOptions, O> optionsModifier) {
        this.optionsModifier = optionsModifier;

        this.token = source.token;
        this.reactorResources = source.reactorResources;
        this.jacksonResources = source.jacksonResources;
        this.exchangeStrategies = source.exchangeStrategies;
        this.responseTransformers = source.responseTransformers;
        this.globalRateLimiter = source.globalRateLimiter;
        this.debugMode = source.debugMode;
    }

    /**
     * Add a configuration for {@link Router} implementation-specific cases, changing the type of the current
     * {@link RouterOptions} object passed to the {@link Router} factory in build methods.
     *
     * @param optionsModifier {@link Function} to transform the {@link RouterOptions} type to provide custom
     * {@link Router} implementations a proper configuration object.
     * @param <O2> new type for the options
     * @return a new {@link DiscordClientBuilder} that will now work with the new options type.
     */
    public <O2 extends RouterOptions> DiscordClientBuilder<O2> setExtraOptions(Function<? super O, O2> optionsModifier) {
        return new DiscordClientBuilder<>(this, this.optionsModifier.andThen(optionsModifier));
    }

    /**
     * Change the token stored in this builder.
     *
     * @param token the new bot token
     * @return this builder
     */
    public DiscordClientBuilder<O> setToken(final String token) {
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
    public DiscordClientBuilder<O> setReactorResources(ReactorResources reactorResources) {
        this.reactorResources = reactorResources;
        return this;
    }

    /**
     * Set a new {@link JacksonResources} to this builder, dedicated to provide an {@link ObjectMapper} for
     * serialization and deserialization of data.
     *
     * @param jacksonResources the new resource provider for serialization and deserialization
     * @return this builder
     */
    public DiscordClientBuilder<O> setJacksonResources(JacksonResources jacksonResources) {
        this.jacksonResources = jacksonResources;
        return this;
    }

    /**
     * Set the strategies to use when reading or writing HTTP request and response body entities. Defaults to using
     * {@link #setJacksonResources(JacksonResources)} to build a {@link ExchangeStrategies#jackson(ObjectMapper)} that
     * is capable of encoding and decoding JSON using Jackson.
     *
     * @param exchangeStrategies the HTTP exchange strategies to use
     * @return this builder
     */
    public DiscordClientBuilder<O> setExchangeStrategies(ExchangeStrategies exchangeStrategies) {
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
     * @param responseFunction the {@link ResponseFunction} to transform the responses from matching requests.
     * @return this builder
     */
    public DiscordClientBuilder<O> onClientResponse(ResponseFunction responseFunction) {
        responseTransformers.add(responseFunction);
        return this;
    }

    /**
     * Define the {@link GlobalRateLimiter} to be applied while configuring the {@link Router} for a client.
     * {@link GlobalRateLimiter} purpose is to coordinate API requests to properly delay them under global rate
     * limiting scenarios. A supporting {@link Router} factory (supplied at {@link #build(Function)}) is responsible
     * for applying the given limiter.
     *
     * @param globalRateLimiter the limiter instance to be used while configuring a {@link Router}
     * @return this builder
     * @see GlobalRateLimiter
     */
    public DiscordClientBuilder<O> setGlobalRateLimiter(GlobalRateLimiter globalRateLimiter) {
        this.globalRateLimiter = globalRateLimiter;
        return this;
    }

    /**
     * Whether to enable {@link Hooks#onOperatorDebug()} when building a {@link DiscordClient}. This is a global hook to
     * enrich stack traces in case of errors for easier debugging at a performance cost. In production or higher load
     * scenarios, we recommend setting this to {@code false} and looking for better alternatives such as the Reactor
     * debug agent.
     *
     * @param debugMode {@code true} to enable debug mode. Setting this to false will not reset the hook if
     * previously enabled.
     * @return this builder
     * @see <a href="https://projectreactor.io/docs/core/release/reference/#reactor-tools-debug">
     * Reactor Reference: Production-ready Global Debugging</a>
     */
    public DiscordClientBuilder<O> setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
    }

    /**
     * Create a client capable of connecting to Discord REST API and to establish Gateway and Voice Gateway connections,
     * using a {@link DefaultRouter} that is capable of working in monolithic environments.
     *
     * @return a configured {@link DiscordClient} based on this builder parameters
     */
    public DiscordClient build() {
        return build(DefaultRouter::new);
    }

    /**
     * Create a client capable of connecting to Discord REST API and to establish Gateway and Voice Gateway connections,
     * using a custom {@link Router} factory. The resulting {@link DiscordClient} will use the produced
     * {@link Router} for every request.
     *
     * @param routerFactory the factory of {@link Router} implementation
     * @return a configured {@link DiscordClient} based on this builder parameters
     */
    public DiscordClient build(Function<O, Router> routerFactory) {
        if (debugMode) {
            Hooks.onOperatorDebug();
        }

        ReactorResources reactor = initReactorResources();
        JacksonResources jackson = initJacksonResources();
        RestClient restClient = new RestClient(routerFactory.apply(buildOptions(reactor, jackson)));
        CoreResources coreResources = new CoreResources(token, restClient, reactor, jackson);

        Properties properties = GitProperties.getProperties();
        String url = properties.getProperty(GitProperties.APPLICATION_URL, "https://discord4j.com");
        String name = properties.getProperty(GitProperties.APPLICATION_NAME, "Discord4J");
        String version = properties.getProperty(GitProperties.APPLICATION_VERSION, "3.1");
        String gitDescribe = properties.getProperty(GitProperties.GIT_COMMIT_ID_DESCRIBE, version);
        log.info("{} {} ({})", name, gitDescribe, url);
        return new DiscordClient(coreResources);
    }

    private O buildOptions(ReactorResources reactor, JacksonResources jackson) {
        RouterOptions options = new RouterOptions(token, reactor, initExchangeStrategies(jackson),
                responseTransformers, initGlobalRateLimiter());
        return this.optionsModifier.apply(options);
    }

    private ReactorResources initReactorResources() {
        if (reactorResources != null) {
            return reactorResources;
        }
        return new ReactorResources();
    }

    private JacksonResources initJacksonResources() {
        if (jacksonResources != null) {
            return jacksonResources;
        }
        return new JacksonResources(mapper -> mapper.addHandler(new UnknownPropertyHandler(true)));
    }

    private ExchangeStrategies initExchangeStrategies(JacksonResources jacksonResources) {
        if (exchangeStrategies != null) {
            return exchangeStrategies;
        }
        return ExchangeStrategies.jackson(jacksonResources.getObjectMapper());
    }

    private GlobalRateLimiter initGlobalRateLimiter() {
        if (globalRateLimiter != null) {
            return globalRateLimiter;
        }
        return new ParallelGlobalRateLimiter(16);
    }
}
