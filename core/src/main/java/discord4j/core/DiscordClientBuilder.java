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
import discord4j.rest.http.client.DiscordWebClient;
import discord4j.rest.request.DefaultRouterFactory;
import discord4j.rest.request.Router;
import discord4j.rest.request.RouterFactory;
import discord4j.rest.request.RouterOptions;
import discord4j.rest.response.ResponseFunction;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Scheduler;
import reactor.netty.http.client.HttpClient;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.Properties;

/**
 * Builder suited for creating a {@link DiscordClient}.
 */
public final class DiscordClientBuilder {

    private static final Logger log = Loggers.getLogger(DiscordClientBuilder.class);

    private String token;
    private ReactorResources reactorResources = null;
    private JacksonResources jacksonResources = null;
    private RouterFactory routerFactory = null;
    private RouterOptions routerOptions = null;
    private boolean debugMode = true;

    /**
     * Initialize a new builder with the given token.
     *
     * @param token the bot token used to authenticate to Discord
     */
    public DiscordClientBuilder(final String token) {
        this.token = Objects.requireNonNull(token);
    }

    /**
     * Change the token stored in this builder.
     *
     * @param token the new bot token
     * @return this builder
     */
    public DiscordClientBuilder setToken(final String token) {
        this.token = Objects.requireNonNull(token);
        return this;
    }

    /**
     * Set a new {@link ReactorResources} dedicated to set up a connection pool, an event pool, as well as the
     * supporting {@link HttpClient} used for making rest requests and maintaining gateway connections.
     *
     * @param reactorResources the new resource provider used for rest and gateway operations, can be {@code
     * null} to use a default value
     * @return this builder
     */
    public DiscordClientBuilder setReactorResources(@Nullable ReactorResources reactorResources) {
        this.reactorResources = reactorResources;
        return this;
    }

    /**
     * Set a new {@link JacksonResources} to this builder, dedicated to provide an {@link ObjectMapper} for
     * serialization and deserialization of data.
     *
     * @param jacksonResources the new resource provider for serialization and deserialization, use {@code null}
     * to use a default one
     * @return this builder
     */
    public DiscordClientBuilder setJacksonResources(@Nullable JacksonResources jacksonResources) {
        this.jacksonResources = jacksonResources;
        return this;
    }

    /**
     * Set a new {@link RouterFactory} used to create a {@link discord4j.rest.request.Router} that executes Discord
     * REST API requests. The resulting client will utilize the produced Router for every request.
     *
     * @param routerFactory a new RouterFactory to create a Router that performs API requests. Pass {@code null} to
     * use a default value
     * @return this builder
     */
    public DiscordClientBuilder setRouterFactory(@Nullable RouterFactory routerFactory) {
        this.routerFactory = routerFactory;
        return this;
    }

    /**
     * Sets a new {@link RouterOptions} used to configure a {@link RouterFactory}.
     * <p>
     * {@code RouterOptions} instances provide a way to override the {@link Scheduler} used for retrieving API responses
     * and scheduling rate limiting actions. It also allows changing the behavior associated with API errors through
     * {@link RouterOptions.Builder#onClientResponse(ResponseFunction)}.
     * <p>
     * If you use a default {@code RouterFactory}, it will use the supplied {@code RouterOptions} to configure itself
     * while building this client.
     *
     * @param routerOptions a new {@code RouterOptions} to configure a {@code RouterFactory}
     * @return this builder
     */
    public DiscordClientBuilder setRouterOptions(@Nullable RouterOptions routerOptions) {
        this.routerOptions = routerOptions;
        return this;
    }

    public DiscordClientBuilder setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
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

    private DiscordWebClient initWebClient(HttpClient httpClient, ObjectMapper mapper) {
        return new DiscordWebClient(httpClient, ExchangeStrategies.jackson(mapper), token);
    }

    private RouterFactory initRouterFactory() {
        if (routerFactory != null) {
            return routerFactory;
        }
        return new DefaultRouterFactory();
    }

    private Router initRouter(RouterFactory factory, DiscordWebClient webClient) {
        if (routerOptions != null) {
            return factory.getRouter(webClient, routerOptions);
        }
        return factory.getRouter(webClient);
    }

    /**
     * Create a client ready to connect to Discord.
     *
     * @return a {@link DiscordClient} based on this bJHuilder parameters
     */
    public DiscordClient build() {
        if (debugMode) {
            Hooks.onOperatorDebug();
        }

        ReactorResources reactor = initReactorResources();
        JacksonResources jackson = initJacksonResources();
        HttpClient httpClient = reactor.getHttpClient();
        DiscordWebClient webClient = initWebClient(httpClient, jackson.getObjectMapper());
        RouterFactory routerFactory = initRouterFactory();
        Router router = initRouter(routerFactory, webClient);
        RestClient restClient = new RestClient(router);
        CoreResources coreResources = new CoreResources(token, restClient, reactor, jackson);

        Properties properties = GitProperties.getProperties();
        String url = properties.getProperty(GitProperties.APPLICATION_URL, "https://discord4j.com");
        String name = properties.getProperty(GitProperties.APPLICATION_NAME, "Discord4J");
        String version = properties.getProperty(GitProperties.APPLICATION_VERSION, "3.1");
        String gitDescribe = properties.getProperty(GitProperties.GIT_COMMIT_ID_DESCRIBE, version);
        log.info("{} {} ({})", name, gitDescribe, url);
        return new DiscordClient(coreResources);
    }
}
