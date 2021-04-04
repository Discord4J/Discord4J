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
import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ApplicationCommandInteractionData;
import discord4j.discordjson.json.InteractionApplicationCommandCallbackData;
import discord4j.discordjson.json.InteractionData;

/**
 * A single interaction coming from Discord. An application command can be reacted upon through this class by first
 * extracting relevant information from the event and then running one of the initial response methods like {@code
 * acknowledge} or {@code reply}.
 *
 * @see Interactions
 */
@Experimental
public interface RestInteraction {

    /**
     * Return the raw data for this interaction.
     *
     * @return an interaction data object
     */
    InteractionData getData();

    /**
     * Return the ID for this interaction.
     *
     * @return this interaction Snowflake ID
     */
    Snowflake getId();

    /**
     * Return the channel ID where this interaction was created.
     *
     * @return this interaction Snowflake channel ID
     */
    Snowflake getChannelId();

    /**
     * Return the raw application command interaction data from this interaction.
     *
     * @return an application command interaction data object
     */
    ApplicationCommandInteractionData getCommandInteractionData();

    /**
     * Build an interaction handler that will acknowledge this request, displaying a loading state.
     *
     * @return a followup handler to continue processing this interaction asynchronously, until the interaction token
     * bound to this interaction expires after 15 minutes.
     */
    FollowupInteractionHandler acknowledge();

    /**
     * Build an interaction handler that will acknowledge this request, displaying a loading state only for the
     * invoking user.
     *
     * @return a followup handler to continue processing this interaction asynchronously, until the interaction token
     * bound to this interaction expires after 15 minutes.
     */
    FollowupInteractionHandler acknowledgeEphemeral();

    /**
     * Build an interaction handler that will produce a text reply to the interaction member.
     *
     * @param content the content to be sent as reply
     * @return a followup handler to continue processing this interaction asynchronously, until the interaction token
     * bound to this interaction expires after 15 minutes.
     */
    FollowupInteractionHandler reply(String content);

    /**
     * Build an interaction handler that will produce a text reply only to the interaction member.
     *
     * @param content the content to be sent as reply
     * @return a followup handler to continue processing this interaction asynchronously, until the interaction token
     * bound to this interaction expires after 15 minutes.
     */
    FollowupInteractionHandler replyEphemeral(String content);

    /**
     * Build an interaction handler that will produce a reply using the contents of the supplied callback data to the
     * interaction member.
     *
     * @param callbackData the data used to produce a reply message
     * @return a followup handler to continue processing this interaction asynchronously, until the interaction token
     * bound to this interaction expires after 15 minutes.
     */
    FollowupInteractionHandler reply(InteractionApplicationCommandCallbackData callbackData);
}
