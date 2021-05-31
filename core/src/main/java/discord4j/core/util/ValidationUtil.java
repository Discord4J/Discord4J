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

package discord4j.core.util;

import discord4j.discordjson.json.gateway.RequestGuildMembers;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;

public class ValidationUtil {

    /**
     * Throws if the request is invalid given the current intents.
     *
     * @param request The request to validate
     * @param possibleIntents The current intents
     * @see
     * <a href="https://discord.com/developers/docs/topics/gateway#request-guild-members">https://discord.com/developers/docs/topics/gateway#request-guild-members</a>
     */
    public static void validateRequestGuildMembers(RequestGuildMembers request, Possible<IntentSet> possibleIntents) {
        if (request.query().isAbsent() == request.userIds().isAbsent()) {
            throw new IllegalArgumentException("One of query or user ids is required.");
        }

        // Further Validation is only required if we are using intents.
        if (possibleIntents.isAbsent()) {
            return;
        }
        IntentSet intents = possibleIntents.get();

        boolean requestingPresences = request.presences().toOptional().orElse(false);
        if (requestingPresences && !intents.contains(Intent.GUILD_PRESENCES)) {
            throw new IllegalArgumentException("GUILD_PRESENCES intent is required to set presences = true.");
        }

        if (isRequestingEntireList(request) && !intents.contains(Intent.GUILD_MEMBERS)) {
            throw new IllegalArgumentException("GUILD_MEMBERS intent is required to request the entire member list");
        }
    }

    /**
     * Return whether the given {@link RequestGuildMembers} instance is requesting an entire guild's list of members.
     *
     * @param request the request to check
     * @return {@code true} if this request will attempt to retrieve the complete list of guild members, and {@code
     * false} otherwise
     */
    public static boolean isRequestingEntireList(RequestGuildMembers request) {
        return request.query().toOptional().map(String::isEmpty).orElse(false) && request.limit() == 0;
    }
}
