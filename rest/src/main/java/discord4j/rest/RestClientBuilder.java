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
package discord4j.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.JacksonResources;
import discord4j.common.ReactorResources;
import discord4j.common.sinks.EmissionStrategy;
import discord4j.common.util.Token;
import discord4j.rest.RestClientBuilder.Resources;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.request.*;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Route;
import discord4j.rest.route.Routes;
import discord4j.rest.util.AllowedMentions;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.annotation.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Builder suited for creating a {@link RestClient}. To acquire an instance, see {@link #createRest(String)}.
 */
public class RestClientBuilder<C extends RestClient, R extends Resources, O extends RouterOptions, B extends RestClientBuilder<C, R, O, B>> {

    protected final Function<B, Mono<Token>> tokenFactory;
    protected final Function<RouterOptions, O> optionsModifier;
    protected final BiFunction<B, Resources, R> resourcesModifier;
    protected final Function<R, C> clientFactory;

    protected AuthorizationScheme authorizationScheme;
    protected ReactorResources reactorResources;
    protected JacksonResources jacksonResources;
    protected ExchangeStrategies exchangeStrategies;
    protected List<ResponseFunction> responseTransformers = new ArrayList<>();
    protected GlobalRateLimiter globalRateLimiter;
    protected RequestQueueFactory requestQueueFactory;
    @Nullable
    protected AllowedMentions allowedMentions;

    /**
     * Initialize a new builder with the given token.
     *
     * @param token the bot token used to authenticate to Discord
     */
    public static <B extends RestClientBuilder<RestClient, Resources, RouterOptions, B>> RestClientBuilder<RestClient, Resources, RouterOptions, B> createRest(String token) {
        Function<Resources, RestClient> clientFactory = resources -> {
            RestResources restResources = new RestResources(resources.getToken(), resources.getReactorResources(),
                    resources.getJacksonResources(), resources.getRouter(), resources.getAllowedMentions().orElse(null));
            return new RestClient(restResources);
        };
        return new RestClientBuilder<>(__ -> Mono.just(Token.of(token)), Function.identity(), (__, resources) -> resources,
                clientFactory);
    }

    protected RestClientBuilder(Function<B, Mono<Token>> tokenFactory,
                                Function<RouterOptions, O> optionsModifier,
                                BiFunction<B, Resources, R> resourcesModifier,
                                Function<R, C> clientFactory) {
        this.tokenFactory = Objects.requireNonNull(tokenFactory, "tokenFactory");
        this.resourcesModifier = Objects.requireNonNull(resourcesModifier, "resourcesModifier");
        this.clientFactory = Objects.requireNonNull(clientFactory, "clientFactory");
        this.optionsModifier = Objects.requireNonNull(optionsModifier, "optionsModifier");
    }

    protected RestClientBuilder(RestClientBuilder<?, ?, ?, ?> source,
                                Function<B, Mono<Token>> tokenFactory,
                                BiFunction<B, Resources, R> resourcesModifier,
                                Function<R, C> clientFactory,
                                Function<RouterOptions, O> optionsModifier) {
        this.tokenFactory = tokenFactory;
        this.resourcesModifier = resourcesModifier;
        this.clientFactory = clientFactory;
        this.optionsModifier = optionsModifier;

        this.reactorResources = source.reactorResources;
        this.jacksonResources = source.jacksonResources;
        this.exchangeStrategies = source.exchangeStrategies;
        this.responseTransformers = source.responseTransformers;
        this.globalRateLimiter = source.globalRateLimiter;
        this.requestQueueFactory = source.requestQueueFactory;
    }

    @SuppressWarnings("unchecked")
    protected B getThis() {
        return (B) this;
    }

    protected <R2 extends R, B2 extends RestClientBuilder<C, R2, O, B2>> RestClientBuilder<C, R2, O, B2> setExtraResources(Function<R, R2> resourcesModifier) {
        BiFunction<B2, Resources, R2> newResourcesModifier = (builder, resources) ->
                builder.resourcesModifier.andThen(resourcesModifier).apply(builder, resources);
        return new RestClientBuilder<>(this, builder -> builder.tokenFactory.apply(builder), newResourcesModifier,
                clientFactory.compose(resourcesModifier), optionsModifier);
    }

    /**
     * Add a configuration for {@link Router} implementation-specific cases, changing the type of the current
     * {@link RouterOptions} object passed to the {@link Router} factory in build methods.
     *
     * @param optionsModifier {@link Function} to transform the {@link RouterOptions} type to provide custom
     * {@link Router} implementations a proper configuration object.
     * @param <O2> new type for the options
     * @return a new {@link RestClientBuilder} that will now work with the new options type.
     */
    public <O2 extends O, B2 extends RestClientBuilder<C, R, O2, B2>> RestClientBuilder<C, R, O2, B2> setExtraOptions(Function<O, O2> optionsModifier) {
        BiFunction<B2, Resources, R> newResourcesModifier = (builder, resources) ->
                builder.resourcesModifier.apply(builder, resources);
        return new RestClientBuilder<>(this, builder -> builder.tokenFactory.apply(builder), newResourcesModifier,
                clientFactory, this.optionsModifier.andThen(optionsModifier));
    }

    /**
     * Set a new {@link ReactorResources} dedicated to set up a connection pool, an event pool, as well as the
     * supporting {@link HttpClient} used for making rest requests and maintaining gateway connections.
     *
     * @param reactorResources the new resource provider used for rest and gateway operations
     * @return this builder
     */
    public B setReactorResources(ReactorResources reactorResources) {
        this.reactorResources = reactorResources;
        return getThis();
    }

    /**
     * Set a new {@link JacksonResources} to this builder, dedicated to provide an {@link ObjectMapper} for
     * serialization and deserialization of data.
     *
     * @param jacksonResources the new resource provider for serialization and deserialization
     * @return this builder
     */
    public B setJacksonResources(JacksonResources jacksonResources) {
        this.jacksonResources = jacksonResources;
        return getThis();
    }

    /**
     * Set the strategies to use when reading or writing HTTP request and response body entities. Defaults to using
     * {@link #setJacksonResources(JacksonResources)} to build a {@link ExchangeStrategies#jackson(ObjectMapper)} that
     * is capable of encoding and decoding JSON using Jackson.
     *
     * @param exchangeStrategies the HTTP exchange strategies to use
     * @return this builder
     */
    public B setExchangeStrategies(ExchangeStrategies exchangeStrategies) {
        this.exchangeStrategies = Objects.requireNonNull(exchangeStrategies, "exchangeStrategies");
        return getThis();
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
    public B onClientResponse(ResponseFunction responseFunction) {
        responseTransformers.add(responseFunction);
        return getThis();
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
    public B setGlobalRateLimiter(GlobalRateLimiter globalRateLimiter) {
        this.globalRateLimiter = globalRateLimiter;
        return getThis();
    }

    /**
     * Sets the {@link RequestQueueFactory} that will provide {@link RequestQueue} instances for the router.
     *
     * <p>
     * If not set, it will use a {@link RequestQueueFactory} providing request queues backed by a sink with
     * reasonable buffering capacity, delaying overflowing requests.
     * </p>
     *
     * @param requestQueueFactory the factory that will provide {@link RequestQueue} instances for the router
     * @return this builder
     * @see RequestQueueFactory#createFromSink(Function, EmissionStrategy)
     */
    public B setRequestQueueFactory(RequestQueueFactory requestQueueFactory) {
        this.requestQueueFactory = requestQueueFactory;
        return getThis();
    }

    /**
     * Sets the {@link AllowedMentions} object that can limit the mentioned target entities that are notified upon
     * message created by this client.
     *
     * @param allowedMentions the options for limiting message mentions. See {@link AllowedMentions#builder()}.
     * @return this builder
     */
    public B setDefaultAllowedMentions(final AllowedMentions allowedMentions) {
        this.allowedMentions = allowedMentions;
        return getThis();
    }

    /**
     * Create a client capable of connecting to Discord REST API using a {@link DefaultRouter} that is capable of
     * working in monolithic environments.
     *
     * @return a configured {@link RestClient} based on this builder parameters
     */
    public C build() {
        return build(DefaultRouter::new);
    }

    /**
     * Create a client capable of connecting to Discord REST API using a custom {@link Router} factory. The resulting
     * {@link RestClient} will use the produced {@link Router} for every request.
     *
     * @param routerFactory the factory of {@link Router} implementation
     * @return a configured {@link RestClient} based on this builder parameters
     */
    public C build(Function<O, Router> routerFactory) {
        Mono<Token> token = tokenFactory.apply(getThis());
        ReactorResources reactor = initReactorResources();
        JacksonResources jackson = initJacksonResources();
        O options = buildOptions(token, reactor, jackson);
        Router router = routerFactory.apply(options);
        R resources = buildResources(token, reactor, jackson, router);
        return clientFactory.apply(resources);
    }

    protected O buildOptions(Mono<Token> token, ReactorResources reactor, JacksonResources jackson) {
        RouterOptions options = new RouterOptions(token, initAuthorizationScheme(), reactor,
                initExchangeStrategies(jackson), responseTransformers, initGlobalRateLimiter(reactor),
                initRequestQueueFactory(), Routes.BASE_URL);
        return optionsModifier.apply(options);
    }

    protected R buildResources(Mono<Token> token, ReactorResources reactor, JacksonResources jackson, Router router) {
        Resources resources = new Resources(token, authorizationScheme, reactor, jackson, exchangeStrategies,
                Collections.unmodifiableList(responseTransformers), globalRateLimiter, router, allowedMentions);
        return resourcesModifier.apply(getThis(), resources);
    }

    protected ReactorResources initReactorResources() {
        return reactorResources != null ? reactorResources : new ReactorResources();
    }

    protected JacksonResources initJacksonResources() {
        return jacksonResources != null ? jacksonResources : JacksonResources.create();
    }

    protected AuthorizationScheme initAuthorizationScheme() {
        return authorizationScheme != null ? authorizationScheme : AuthorizationScheme.BOT;
    }

    protected ExchangeStrategies initExchangeStrategies(JacksonResources jacksonResources) {
        return exchangeStrategies != null ? exchangeStrategies :
                ExchangeStrategies.jackson(jacksonResources.getObjectMapper());
    }

    protected GlobalRateLimiter initGlobalRateLimiter(ReactorResources reactorResources) {
        return globalRateLimiter != null ? globalRateLimiter :
                BucketGlobalRateLimiter.create(50, Duration.ofSeconds(1), reactorResources.getTimerTaskScheduler());
    }

    protected RequestQueueFactory initRequestQueueFactory() {
        return requestQueueFactory != null ? requestQueueFactory : RequestQueueFactory.buffering();
    }

    public static class Resources {

        private final Mono<Token> token;
        private final AuthorizationScheme authorizationScheme;
        private final ReactorResources reactorResources;
        private final JacksonResources jacksonResources;
        private final ExchangeStrategies exchangeStrategies;
        private final List<ResponseFunction> responseTransformers;
        private final GlobalRateLimiter globalRateLimiter;
        private final Router router;
        private final AllowedMentions allowedMentions;

        public Resources(Mono<Token> token, AuthorizationScheme authorizationScheme, ReactorResources reactorResources,
                         JacksonResources jacksonResources, ExchangeStrategies exchangeStrategies,
                         List<ResponseFunction> responseTransformers, GlobalRateLimiter globalRateLimiter, Router router,
                         @Nullable AllowedMentions allowedMentions) {
            this.token = token;
            this.authorizationScheme = authorizationScheme;
            this.reactorResources = reactorResources;
            this.jacksonResources = jacksonResources;
            this.exchangeStrategies = exchangeStrategies;
            this.responseTransformers = responseTransformers;
            this.globalRateLimiter = globalRateLimiter;
            this.router = router;
            this.allowedMentions = allowedMentions;
        }

        public Mono<Token> getToken() {
            return token;
        }

        public AuthorizationScheme getAuthorizationScheme() {
            return authorizationScheme;
        }

        public ReactorResources getReactorResources() {
            return reactorResources;
        }

        public JacksonResources getJacksonResources() {
            return jacksonResources;
        }

        public ExchangeStrategies getExchangeStrategies() {
            return exchangeStrategies;
        }

        public List<ResponseFunction> getResponseTransformers() {
            return responseTransformers;
        }

        public GlobalRateLimiter getGlobalRateLimiter() {
            return globalRateLimiter;
        }

        public Router getRouter() {
            return router;
        }

        public Optional<AllowedMentions> getAllowedMentions() {
            return Optional.ofNullable(allowedMentions);
        }
    }
}
