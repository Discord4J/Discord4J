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

package discord4j.core;

import discord4j.common.JacksonResources;
import discord4j.common.ReactorResources;
import discord4j.rest.RestClient;

/**
 * A set of resources required to build {@link DiscordClient} instances and are used for core Discord4J operations
 * like entity manipulation and API communication.
 */
public class CoreResources {

    private final String token;
    private final RestClient restClient;
    private final ReactorResources reactorResources;
    private final JacksonResources jacksonResources;

    /**
     * Create a {@link CoreResources} instance with the given resources.
     *
     * @param token the bot token used to authenticate requests
     * @param restClient a client to perform REST API actions
     * @param reactorResources Reactor resources to establish connections and schedule tasks
     * @param jacksonResources Jackson data-binding resources to map objects
     */
    public CoreResources(String token, RestClient restClient, ReactorResources reactorResources,
                         JacksonResources jacksonResources) {
        this.token = token;
        this.restClient = restClient;
        this.reactorResources = reactorResources;
        this.jacksonResources = jacksonResources;
    }

    /**
     * Return the bot token used to authenticate requests.
     *
     * @return the bot token
     */
    public String getToken() {
        return token;
    }

    /**
     * A client that enables performing REST API requests.
     *
     * @return a configured {@link RestClient} instance
     */
    public RestClient getRestClient() {
        return restClient;
    }

    /**
     * Return Reactor resources to establish connections and schedule tasks.
     *
     * @return a configured {@link ReactorResources} instance
     */
    public ReactorResources getReactorResources() {
        return reactorResources;
    }

    /**
     * Return Jackson resources to transform objects.
     *
     * @return a configured {@link JacksonResources} instance
     */
    public JacksonResources getJacksonResources() {
        return jacksonResources;
    }
}
