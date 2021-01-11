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
import discord4j.discordjson.json.MemberData;

/**
 * A single interaction coming from Discord. An application command can be reacted upon through this class by first
 * extracting relevant information from the event and then running one of the initial response methods like {@code
 * acknowledge} or {@code reply}.
 *
 * @see Interactions
 */
@Experimental
public interface Interaction {

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
     * Return the guild ID where this interaction was created.
     *
     * @return this interaction Snowflake guild ID
     */
    Snowflake getGuildId();

    /**
     * Return the channel ID where this interaction was created.
     *
     * @return this interaction Snowflake channel ID
     */
    Snowflake getChannelId();

    /**
     * Return the raw member data that created this interaction.
     *
     * @return a member data object
     */
    MemberData getMemberData();

    /**
     * Return this interaction member.
     *
     * @return an object with methods to operate on this interaction member
     */
    InteractionMember getInteractionMember();

    /**
     * Return the raw application command interaction data from this interaction.
     *
     * @return an application command interaction data object
     */
    ApplicationCommandInteractionData getCommandInteractionData();

    /**
     * Build an interaction handler that will acknowledge this request, without displaying the source.
     *
     * @return a followup handler to continue processing this interaction asynchronously, until the interaction token
     * bound to this interaction expires after 15 minutes.
     */
    FollowupInteractionHandler acknowledge();

    /**
     * Build an interaction handler that will acknowledge this request, optionally displaying the source.
     *
     * @param withSource whether to display the source message
     * @return a followup handler to continue processing this interaction asynchronously, until the interaction token
     * bound to this interaction expires after 15 minutes.
     */
    FollowupInteractionHandler acknowledge(boolean withSource);

    /**
     * Build an interaction handler that will produce a text reply to the interaction member, optionally displaying
     * the source message.
     *
     * @param content the content to be sent as reply
     * @param withSource whether to display the source message
     * @return a followup handler to continue processing this interaction asynchronously, until the interaction token
     * bound to this interaction expires after 15 minutes.
     */
    FollowupInteractionHandler reply(String content, boolean withSource);

    /**
     * Build an interaction handler that will produce a reply using the contents of the supplied callback data to the
     * interaction member, optionally displaying the source message.
     *
     * @param callbackData the data used to produce a reply message
     * @param withSource whether to display the source message
     * @return a followup handler to continue processing this interaction asynchronously, until the interaction token
     * bound to this interaction expires after 15 minutes.
     */
    FollowupInteractionHandler reply(InteractionApplicationCommandCallbackData callbackData, boolean withSource);
}
