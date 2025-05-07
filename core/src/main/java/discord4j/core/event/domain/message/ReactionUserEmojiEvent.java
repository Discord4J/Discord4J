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

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.Reaction;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

/**
 * Represents an event related to a {@link discord4j.core.object.reaction.Reaction} with a user and more data of the reaction.
 */
public class ReactionUserEmojiEvent extends ReactionBaseEmojiEvent {

    private final long userId;
    private final boolean burst;
    private final int type;

    public ReactionUserEmojiEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long channelId, long messageId, @Nullable Long guildId, Emoji emoji, long userId, boolean burst, int type) {
        super(gateway, shardInfo, channelId, messageId, guildId, emoji);
        this.userId = userId;
        this.burst = burst;
        this.type = type;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link User} related to this event.
     *
     * @return The Id of the {@link User} related to this reaction.
     */
    public Snowflake getUserId() {
        return Snowflake.of(userId);
    }

    /**
     * Requests to retrieve the {@link User} related to this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} related to the reaction.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser() {
        return getClient().getUserById(getUserId());
    }

    /**
     * Gets whether the reaction related to this event is "Super".
     *
     * @return Whether the reaction related to this event is "Super".
     */
    public boolean isSuperReaction() {
        return this.burst;
    }

    /**
     * Gets the {@link Reaction.Type} of the reaction.
     *
     * @return A {@link Reaction.Type}
     */
    public Reaction.Type getType() {
        return Reaction.Type.of(this.type);
    }

    @Override
    public String toString() {
        return "ReactionUserEmojiEvent{" +
            "userId=" + userId +
            ", burst=" + burst +
            ", type=" + type +
            "} " + super.toString();
    }
}
