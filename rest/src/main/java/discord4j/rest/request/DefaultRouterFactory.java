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

import discord4j.rest.http.client.DiscordWebClient;
import reactor.core.scheduler.Scheduler;

/**
 * A monolithic {@link RouterFactory} that can build {@link Router} instances to execute Discord API requests.
 * <p>
 * This factory creates a new instance each time so it is not fit for coordinating sharding requests. For those cases,
 * see {@link SingleRouterFactory}.
 */
public class DefaultRouterFactory implements RouterFactory {

    private final RouterOptions routerOptions;

    public DefaultRouterFactory() {
        this.routerOptions = RouterOptions.builder().build();
    }

    @Deprecated
    public DefaultRouterFactory(Scheduler responseScheduler, Scheduler rateLimitScheduler) {
        this.routerOptions = RouterOptions.builder()
                .responseScheduler(responseScheduler)
                .rateLimitScheduler(rateLimitScheduler)
                .build();
    }

    public DefaultRouterFactory(RouterOptions routerOptions) {
        this.routerOptions = routerOptions;
    }

    @Override
    public Router getRouter(DiscordWebClient webClient) {
        return new DefaultRouter(webClient, routerOptions);
    }

    @Override
    public Router getRouter(DiscordWebClient webClient, RouterOptions routerOptions) {
        return new DefaultRouter(webClient, routerOptions);
    }
}
