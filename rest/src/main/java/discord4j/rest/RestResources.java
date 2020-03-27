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

package discord4j.rest;

import discord4j.common.JacksonResources;
import discord4j.common.ReactorResources;
import discord4j.rest.request.Router;

/**
 * A set of resources required for key Discord4J features like entity manipulation and API communication.
 */
public class RestResources {

    private final String token;
    private final ReactorResources reactorResources;
    private final JacksonResources jacksonResources;
    private final Router router;

    /**
     * Create a {@link RestResources} instance with the given resources.
     *
     * @param token the bot token used to authenticate requests
     * @param reactorResources Reactor resources to establish connections and schedule tasks
     * @param jacksonResources Jackson data-binding resources to map objects
     * @param router a connector to perform requests against Discord API
     */
    public RestResources(String token, ReactorResources reactorResources, JacksonResources jacksonResources,
                         Router router) {
        this.token = token;
        this.reactorResources = reactorResources;
        this.jacksonResources = jacksonResources;
        this.router = router;
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

    /**
     * Return the {@link Router} tied to this resources object.
     *
     * @return a configured {@link Router} instance
     */
    public Router getRouter() {
        return router;
    }
}
