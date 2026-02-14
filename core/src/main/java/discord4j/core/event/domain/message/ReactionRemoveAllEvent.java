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
package discord4j.core.event.domain.message;

import discord4j.core.GatewayDiscordClient;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;

/**
 * Dispatched when all the reactions on a message are removed.
 * <p>
 * {@link #getGuildId()} may not be present if the message was in a private channel.
 * <p>
 * Corresponding {@link ReactionRemoveEvent reactions removes} are NOT dispatched
 * for messages included in this event.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/events/gateway-events#message-reaction-remove-all">Message Reaction
 * Remove All</a>
 */
public class ReactionRemoveAllEvent extends ReactionEvent {

    public ReactionRemoveAllEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long channelId, long messageId,
                                  @Nullable Long guildId) {
        super(gateway, shardInfo, channelId, messageId, guildId);
    }

    @Override
    public String toString() {
        return "ReactionRemoveAllEvent{" +
            "} " + super.toString();
    }
}
