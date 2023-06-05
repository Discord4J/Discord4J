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
import discord4j.common.util.Snowflake;
import discord4j.common.util.TokenUtil;
import discord4j.rest.http.client.AuthorizationScheme;
import discord4j.rest.request.Router;
import discord4j.rest.util.AllowedMentions;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * A set of resources required for key Discord4J features like entity manipulation and API communication.
 */
public class RestResources {

    private final AuthorizationScheme scheme;
    private final String token;
    private final ReactorResources reactorResources;
    private final JacksonResources jacksonResources;
    private final Router router;
    @Nullable
    private final Long selfId;
    @Nullable
    private final AllowedMentions allowedMentions;

    public RestResources(String token, ReactorResources reactorResources, JacksonResources jacksonResources,
                         Router router, @Nullable AllowedMentions allowedMentions) {
        this(AuthorizationScheme.BOT, token, reactorResources, jacksonResources, router, allowedMentions);
    }

    /**
     * Create a {@link RestResources} instance with the given resources.
     *
     * @param token the bot token used to authenticate requests
     * @param reactorResources Reactor resources to establish connections and schedule tasks
     * @param jacksonResources Jackson data-binding resources to map objects
     * @param router a connector to perform requests against Discord API
     * @param allowedMentions a configuration object to limit mentions creating notifications on message sending
     */
    public RestResources(AuthorizationScheme scheme, String token, ReactorResources reactorResources,
                         JacksonResources jacksonResources, Router router, @Nullable AllowedMentions allowedMentions) {
        this.scheme = scheme;
        this.token = token;
        this.reactorResources = reactorResources;
        this.jacksonResources = jacksonResources;
        this.router = router;
        this.selfId = scheme == AuthorizationScheme.BOT ? TokenUtil.getSelfId(token) : null;
        this.allowedMentions = allowedMentions;
    }

    public AuthorizationScheme getScheme() {
        return scheme;
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

    /**
     * Gets the bot user's ID.
     *
     * @return The bot user's ID.
     */
    public Snowflake getSelfId() {
        return Snowflake.of(Optional.ofNullable(selfId).orElseThrow(UnsupportedOperationException::new));
    }

    /**
     * Return the configured {@link AllowedMentions}, if present.
     *
     * @return the configured allowed mentions setting or empty Optional if none was configured
     */
    public Optional<AllowedMentions> getAllowedMentions() {
        return Optional.ofNullable(allowedMentions);
    }
}
