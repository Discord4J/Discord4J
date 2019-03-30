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

/**
 * A monolithic {@link discord4j.rest.request.RouterFactory} that caches a {@link discord4j.rest.request.Router}
 * captured at instantiation instead of producing a new one each time
 * {@link #getRouter(discord4j.rest.http.client.DiscordWebClient)} or
 * {@link #getRouter(DiscordWebClient, RouterOptions)} is called.
 * <p>
 * Suited for sharing a router in order to coordinate its work across shards. It is not suited for distributed scenarios
 * where multiple shards exist across processes unless care is taken to properly coordinate each request externally.
 */
public class SingleRouterFactory implements RouterFactory {

    private final Router router;

    public SingleRouterFactory(Router router) {
        this.router = router;
    }

    @Override
    public Router getRouter(DiscordWebClient httpClient) {
        return router;
    }

    @Override
    public Router getRouter(DiscordWebClient httpClient, RouterOptions routerOptions) {
        return router;
    }
}
