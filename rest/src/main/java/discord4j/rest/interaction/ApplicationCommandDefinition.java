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

package discord4j.rest.interaction;

import discord4j.common.annotations.Experimental;
import discord4j.discordjson.json.ApplicationCommandInteractionData;

/**
 * Represents an application command that can be tested against incoming interactions and to build a response sequence.
 */
@Experimental
public interface ApplicationCommandDefinition {

    /**
     * Match whether the incoming interaction can be handled by this command.
     *
     * @param acid the incoming interaction data
     * @return {@code true} if this command can handle this interaction, {@code false} otherwise
     */
    boolean test(ApplicationCommandInteractionData acid);

    /**
     * Return the actual component responsible for maintaining interaction responses.
     *
     * @param interaction the interaction this command is handling
     * @return a source for responses around the given interaction
     */
    InteractionHandler createResponseHandler(RestInteraction interaction);
}
