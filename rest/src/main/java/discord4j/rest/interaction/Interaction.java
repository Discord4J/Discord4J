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

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ApplicationCommandInteractionData;
import discord4j.discordjson.json.InteractionApplicationCommandCallbackData;
import discord4j.discordjson.json.InteractionData;
import discord4j.discordjson.json.MemberData;

public interface Interaction {

    InteractionData getData();

    Snowflake getId();

    Snowflake getGuildId();

    Snowflake getChannelId();

    MemberData getMemberData();

    ApplicationCommandInteractionData getCommandInteractionData();

    FollowupInteractionHandler acknowledge();

    FollowupInteractionHandler acknowledge(boolean withSource);

    FollowupInteractionHandler reply(String content, boolean withSource);

    FollowupInteractionHandler reply(InteractionApplicationCommandCallbackData callbackData, boolean withSource);
}
