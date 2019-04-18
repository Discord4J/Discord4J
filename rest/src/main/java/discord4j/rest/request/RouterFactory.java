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
 * Factory used to produce {@link discord4j.rest.request.Router} instances dedicated to execute API requests.
 */
public interface RouterFactory {

    /**
     * Retrieve a {@link discord4j.rest.request.Router} configured to process API requests.
     *
     * @param webClient a web client to parameterize the {@link Router} creation
     * @return a {@link Router} prepared to process API requests
     */
    Router getRouter(DiscordWebClient webClient);

    /**
     * Retrieve a {@link discord4j.rest.request.Router} that can be further configured with the given
     * {@link RouterOptions} to process API requests.
     *
     * @param webClient a web client to parameterize the {@link Router} creation
     * @param routerOptions a configuration object to control the behavior of the resulting {@link Router}
     * @return a {@link Router} prepared to process API requests
     */
    Router getRouter(DiscordWebClient webClient, RouterOptions routerOptions);
}
