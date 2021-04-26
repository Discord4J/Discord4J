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

package discord4j.oauth2;

import discord4j.common.JacksonResources;
import discord4j.common.ReactorResources;
import discord4j.common.util.Token;
import discord4j.discordjson.json.AccessTokenData;
import discord4j.oauth2.OAuth2ClientBuilder.Resources;
import discord4j.oauth2.object.AccessToken;
import discord4j.oauth2.request.AuthorizationCodeGrantRequest;
import discord4j.oauth2.request.ClientCredentialsGrantRequest;
import discord4j.oauth2.service.OAuth2Service;
import discord4j.oauth2.spec.AuthorizationCodeGrantSpec;
import discord4j.oauth2.spec.ClientCredentialsGrantSpec;
import discord4j.rest.RestClientBuilder;
import discord4j.rest.RestResources;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.request.*;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import discord4j.rest.util.AllowedMentions;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Builder suited for creating a {@link OAuth2Client}. To acquire an instance, see {@link #createFromCode(Consumer)} or
 * {@link #createFromCredentials(Consumer)}.
 */
public class OAuth2ClientBuilder<C extends OAuth2Client, R extends Resources, O extends RouterOptions, B extends OAuth2ClientBuilder<C, R, O, B>> extends RestClientBuilder<C, R, O, B> {

    protected OAuth2Service service;
    protected long clientId;
    protected String clientSecret;

    private static <B extends OAuth2ClientBuilder<OAuth2Client, Resources, RouterOptions, B>> BiFunction<B, RestClientBuilder.Resources, Resources> getResourcesModifier() {
        return (builder, resources) -> new Resources(resources.getToken(), resources.getAuthorizationScheme(),
                resources.getReactorResources(), resources.getJacksonResources(), resources.getExchangeStrategies(),
                resources.getResponseTransformers(), resources.getGlobalRateLimiter(), resources.getRouter(),
                resources.getAllowedMentions().orElse(null), builder.service, builder.clientId, builder.clientSecret);
    }

    private static Function<Resources, OAuth2Client> getClientFactory() {
        return resources -> {
            RestResources restResources = new RestResources(resources.getToken(), resources.getReactorResources(),
                    resources.getJacksonResources(), resources.getRouter(), resources.getAllowedMentions().orElse(null));
            return new OAuth2Client(restResources, resources.service, resources.clientId, resources.clientSecret);
        };
    }

    /**
     * Initialize a new builder with an {@link AccessToken} exchanged via an authorization code produced from the
     * modified spec.
     *
     * @param spec a {@link Consumer} that provides a "blank" {@link AuthorizationCodeGrantSpec} to be operated on
     */
    public static <B extends OAuth2ClientBuilder<OAuth2Client, Resources, RouterOptions, B>> OAuth2ClientBuilder<OAuth2Client, Resources, RouterOptions, B> createFromCode(Consumer<? super AuthorizationCodeGrantSpec> spec) {
        AuthorizationCodeGrantSpec mutatedSpec = new AuthorizationCodeGrantSpec();
        spec.accept(mutatedSpec);
        Function<B, AuthorizationCodeGrantRequest> requestFactory = builder -> {
            if (builder.clientId != 0) {
                mutatedSpec.setClientId(builder.clientId);
            }
            if (builder.clientSecret != null) {
                mutatedSpec.setClientSecret(builder.clientSecret);
            }
            return mutatedSpec.asRequest();
        };
        Function<B, Mono<Token>> tokenFactory = builder -> {
            AuthorizationCodeGrantRequest request = requestFactory.apply(builder);
            builder.setClientId(request.clientId()).setClientSecret(request.clientSecret());
            return builder.service.exchangeAuthorizationCode(request)
                    .map(data -> new AccessToken(builder.service, builder.clientId, builder.clientSecret, data));
        };
        return new OAuth2ClientBuilder<>(tokenFactory, Function.identity(), getResourcesModifier(), getClientFactory());
    }

    /**
     * Initialize a new builder with an {@link AccessToken} exchanged via client credentials produced from the modified
     * spec.
     *
     * @param spec a {@link Consumer} that provides a "blank" {@link ClientCredentialsGrantSpec} to be operated on
     */
    public static <B extends OAuth2ClientBuilder<OAuth2Client, Resources, RouterOptions, B>> OAuth2ClientBuilder<OAuth2Client, Resources, RouterOptions, B> createFromCredentials(Consumer<? super ClientCredentialsGrantSpec> spec) {
        ClientCredentialsGrantSpec mutatedSpec = new ClientCredentialsGrantSpec();
        spec.accept(mutatedSpec);
        Function<B, ClientCredentialsGrantRequest> requestFactory = builder -> {
            if (builder.clientId != 0) {
                mutatedSpec.setClientId(builder.clientId);
            }
            if (builder.clientSecret != null) {
                mutatedSpec.setClientSecret(builder.clientSecret);
            }
            return mutatedSpec.asRequest();
        };
        Function<B, Mono<Token>> tokenFactory = builder -> {
            ClientCredentialsGrantRequest request = requestFactory.apply(builder);
            builder.setClientId(request.clientId()).setClientSecret(request.clientSecret());
            return builder.service.exchangeClientCredentials(request)
                    .map(data -> new AccessToken(builder.service, builder.clientId, builder.clientSecret, data));
        };
        return new OAuth2ClientBuilder<>(tokenFactory, Function.identity(), getResourcesModifier(), getClientFactory());
    }

    /**
     * Initialize a new builder with a given {@link AccessTokenData} retrieved from an {@link OAuth2Server}.
     *
     * @param data access token data response retrieved from Discord via an {@link OAuth2Server}
     */
    public static <B extends OAuth2ClientBuilder<OAuth2Client, Resources, RouterOptions, B>> OAuth2ClientBuilder<OAuth2Client, Resources, RouterOptions, B> createFromData(AccessTokenData data) {
        Function<B, Mono<Token>> tokenFactory = builder ->
                Mono.just(new AccessToken(builder.service, builder.clientId, builder.clientSecret, data));
        return new OAuth2ClientBuilder<>(tokenFactory, Function.identity(), getResourcesModifier(), getClientFactory());

    }

    protected OAuth2ClientBuilder(Function<B, Mono<Token>> tokenFactory,
                                  Function<RouterOptions, O> optionsModifier,
                                  BiFunction<B, RestClientBuilder.Resources, R> resourcesModifier,
                                  Function<R, C> clientFactory) {
        super(tokenFactory, optionsModifier, resourcesModifier, clientFactory);
    }

    /**
     * Set the client ID of the Discord application associated with the resulting {@link OAuth2Client}.
     *
     * @param clientId the client ID obtained from the Discord developer portal
     * @return this builder
     */
    public B setClientId(final long clientId) {
        this.clientId = clientId;
        return getThis();
    }

    /**
     * Set the client secret of the Discord application associated with the resulting {@link OAuth2Client}.
     *
     * @param clientSecret the client secret obtained from the Discord developer portal
     * @return this builder
     */
    public B setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
        return getThis();
    }

    /**
     * Create a client capable of connecting to Discord REST API on behalf of a user account using a
     * {@link DefaultRouter} that is capable of working in monolithic environments.
     *
     * @return a configured {@link OAuth2Client} based on this builder parameters
     */
    public C build() {
        return build(DefaultRouter::new);
    }

    /**
     * Create a client capable of connecting to Discord REST API on behalf of a user account using a custom
     * {@link Router} factory. The resulting {@link OAuth2Client} will use the produced {@link Router} for every request.
     *
     * @param routerFactory the factory of {@link Router} implementation
     * @return a configured {@link OAuth2Client} based on this builder parameters
     */
    public C build(Function<O, Router> routerFactory) {
        ReactorResources reactor = initReactorResources();
        JacksonResources jackson = initJacksonResources();
        O unauthenticatedOptions = buildUnauthenticatedOptions(reactor, jackson);
        Router unauthenticatedRouter = routerFactory.apply(unauthenticatedOptions);
        service = new OAuth2Service(unauthenticatedRouter);
        Mono<Token> token = tokenFactory.apply(getThis());
        O options = buildOptions(token, reactor, jackson);
        Router router = routerFactory.apply(options);
        R resources = buildResources(token, reactor, jackson, router);
        return clientFactory.apply(resources);
    }

    protected O buildOptions(Mono<Token> token, ReactorResources reactor, JacksonResources jackson) {
        RouterOptions options = new RouterOptions(token, initAuthorizationScheme(), reactor,
                exchangeStrategies, responseTransformers, globalRateLimiter, requestQueueFactory, Routes.BASE_URL);
        return optionsModifier.apply(options);
    }

    protected O buildUnauthenticatedOptions(ReactorResources reactor, JacksonResources jackson) {
        RouterOptions options = new RouterOptions(Mono.empty(), AuthorizationScheme.NONE, reactor,
                exchangeStrategies = initExchangeStrategies(jackson), responseTransformers,
                globalRateLimiter = initGlobalRateLimiter(reactor), requestQueueFactory = initRequestQueueFactory(),
                Routes.BASE_URL);
        return optionsModifier.apply(options);
    }

    protected AuthorizationScheme initAuthorizationScheme() {
        return authorizationScheme != null ? authorizationScheme : AuthorizationScheme.BEARER;
    }

    public static class Resources extends RestClientBuilder.Resources {

        private final OAuth2Service service;
        private final long clientId;
        private final String clientSecret;

        public Resources(Mono<Token> token, AuthorizationScheme authorizationScheme, ReactorResources reactorResources,
                         JacksonResources jacksonResources, ExchangeStrategies exchangeStrategies,
                         List<ResponseFunction> responseTransformers, GlobalRateLimiter globalRateLimiter,
                         Router router, @Nullable AllowedMentions allowedMentions, OAuth2Service service, long clientId,
                         String clientSecret) {
            super(token, authorizationScheme, reactorResources, jacksonResources, exchangeStrategies,
                    responseTransformers, globalRateLimiter, router, allowedMentions);
            this.service = service;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
        }

        public OAuth2Service getService() {
            return service;
        }

        public long getClientId() {
            return clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }
    }
}
