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
import discord4j.common.util.Token;
import discord4j.common.util.TokenUtil;
import discord4j.rest.request.Router;
import discord4j.rest.util.AllowedMentions;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * A set of resources required for key Discord4J features like entity manipulation and API communication.
 */
public class RestResources {

    private final Mono<Token> token;
    private final ReactorResources reactorResources;
    private final JacksonResources jacksonResources;
    private final Router router;
    private final Mono<Long> selfId;
    @Nullable
    private final AllowedMentions allowedMentions;

    /**
     * Create a {@link RestResources} instance with the given resources.
     *
     * @param token the token used to authenticate requests
     * @param reactorResources Reactor resources to establish connections and schedule tasks
     * @param jacksonResources Jackson data-binding resources to map objects
     * @param router a connector to perform requests against Discord API
     * @param allowedMentions a configuration object to limit mentions creating notifications on message sending
     */
    public RestResources(Mono<Token> token, ReactorResources reactorResources, JacksonResources jacksonResources,
                         Router router, @Nullable AllowedMentions allowedMentions) {
        this.token = token;
        this.reactorResources = reactorResources;
        this.jacksonResources = jacksonResources;
        this.router = router;
        this.selfId = token.map(TokenUtil::getSelfId);
        this.allowedMentions = allowedMentions;
    }

    /**
     * Return the token used to authenticate requests.
     *
     * @return the token
     */
    public Mono<Token> getToken() {
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
     * Gets the current user's ID.
     *
     * @return The current user's ID.
     */
    public Mono<Snowflake> getSelfId() {
        return selfId.map(Snowflake::of);
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
