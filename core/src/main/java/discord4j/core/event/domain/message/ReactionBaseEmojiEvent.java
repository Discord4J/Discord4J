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
package discord4j.core.event.domain.message;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.entity.Message;
import discord4j.gateway.ShardInfo;
import reactor.util.annotation.Nullable;

/**
 * Represents an event related to a {@link discord4j.core.object.reaction.Reaction} with an Emoji.
 */
public abstract class ReactionBaseEmojiEvent extends ReactionEvent {

    private final Emoji emoji;

    public ReactionBaseEmojiEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long channelId, long messageId,
                                  @Nullable Long guildId, Emoji emoji) {
        super(gateway, shardInfo, channelId, messageId, guildId);
        this.emoji = emoji;
    }

    /**
     * Gets the {@link Emoji} in the {@link Message} related in this event.
     *
     * @return The {@code Emoji} of to the {@link Message} as a reaction.
     */
    public Emoji getEmoji() {
        return emoji;
    }

    @Override
    public String toString() {
        return "ReactionEmojiEvent{" +
            ", emoji=" + emoji +
            "} " + super.toString();
    }
}
