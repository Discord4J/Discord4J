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

import discord4j.common.GitProperties;
import discord4j.common.util.Token;
import discord4j.rest.RestClientBuilder;
import discord4j.rest.RestClientBuilder.Resources;
import discord4j.rest.request.DefaultRouter;
import discord4j.rest.request.Router;
import discord4j.rest.request.RouterOptions;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Builder suited for creating a {@link DiscordClient}. To acquire an instance, see {@link #create(String)}.
 */
public final class DiscordClientBuilder<C extends DiscordClient, R extends Resources, O extends RouterOptions, B extends DiscordClientBuilder<C, R, O, B>> extends RestClientBuilder<C, R, O, B> {

    private static final Logger log = Loggers.getLogger(DiscordClientBuilder.class);

    /**
     * Initialize a new builder with the given token.
     *
     * @param token the bot token used to authenticate to Discord
     */
    public static <B extends DiscordClientBuilder<DiscordClient, Resources, RouterOptions, B>> DiscordClientBuilder<DiscordClient, Resources, RouterOptions, B> create(String token) {
        Function<Resources, DiscordClient> clientFactory = resources -> {
            CoreResources coreResources = new CoreResources(resources.getToken(), resources.getReactorResources(),
                    resources.getJacksonResources(), resources.getRouter(), resources.getAllowedMentions().orElse(null));
            Properties properties = GitProperties.getProperties();
            String url = properties.getProperty(GitProperties.APPLICATION_URL, "https://discord4j.com");
            String name = properties.getProperty(GitProperties.APPLICATION_NAME, "Discord4J");
            String version = properties.getProperty(GitProperties.APPLICATION_VERSION, "3.2");
            String gitDescribe = properties.getProperty(GitProperties.GIT_COMMIT_ID_DESCRIBE, version);
            log.info("{} {} ({})", name, gitDescribe, url);
            return new DiscordClient(coreResources);
        };
        return new DiscordClientBuilder<>(__ -> Mono.just(Token.of(token)), (__, resources) -> resources, clientFactory,
                Function.identity());
    }

    DiscordClientBuilder(Function<B, Mono<Token>> tokenFactory,
                         BiFunction<B, Resources, R> resourcesModifier,
                         Function<R, C> allocator,
                         Function<RouterOptions, O> optionsModifier) {
        super(tokenFactory, optionsModifier, resourcesModifier, allocator);
    }

    /**
     * Create a client capable of connecting to Discord REST API and to establish Gateway and Voice Gateway connections,
     * using a {@link DefaultRouter} that is capable of working in monolithic environments.
     *
     * @return a configured {@link DiscordClient} based on this builder parameters
     */
    public C build() {
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
    public C build(Function<O, Router> routerFactory) {
        return super.build(routerFactory);
    }
}
