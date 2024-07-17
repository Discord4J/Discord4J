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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.event.domain.poll;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.gateway.PollVoteRemove;
import discord4j.gateway.ShardInfo;

/**
 * Dispatched when a vote is removed from a poll.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway-events#message-poll-vote-remove">Message Poll Vote Remove</a>
 */
public class PollVoteRemoveEvent extends PollVoteEvent {

    public PollVoteRemoveEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, PollVoteRemove data) {
        super(gateway, shardInfo, Snowflake.of(data.userId()), Snowflake.of(data.channelId()), Snowflake.of(data.messageId()), data.guildId().map(Snowflake::of), data.answerId());
    }

}
