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
import discord4j.common.util.Token;
import discord4j.rest.RestResources;
import discord4j.rest.request.Router;
import discord4j.rest.util.AllowedMentions;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

/**
 * A set of resources required to build {@link DiscordClient} instances and are used for core Discord4J operations
 * like entity manipulation and API communication.
 */
public class CoreResources extends RestResources {

    /**
     * Create a {@link CoreResources} instance with the given resources.
     *
     * @param token the bot token used to authenticate requests
     * @param reactorResources Reactor resources to establish connections and schedule tasks
     * @param jacksonResources Jackson data-binding resources to map objects
     * @param router a connector to perform requests against Discord API
     * @param allowedMentions a configuration object to limit mentions creating notifications on message sending
     */
    public CoreResources(Mono<Token> token, ReactorResources reactorResources, JacksonResources jacksonResources,
                         Router router, @Nullable AllowedMentions allowedMentions) {
        super(token, reactorResources, jacksonResources, router, allowedMentions);
    }
}
