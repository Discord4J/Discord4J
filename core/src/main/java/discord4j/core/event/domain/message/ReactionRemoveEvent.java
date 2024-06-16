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
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.Reaction;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.common.util.Snowflake;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when a reaction is removed on a message.
 * <p>
 * {@link #guildId} may not be present if the message was in a private channel.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#message-reaction-remove">Message Reaction
 * Remove</a>
 */
public class ReactionRemoveEvent extends MessageEvent {

    private final long userId;
    private final long channelId;
    private final long messageId;
    @Nullable
    private final Long guildId;
    private final ReactionEmoji emoji;
    private final boolean burst;
    private final int type;

    public ReactionRemoveEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long userId, long channelId, long messageId,
                               @Nullable Long guildId, ReactionEmoji emoji, boolean burst, int type) {
        super(gateway, shardInfo);
        this.userId = userId;
        this.channelId = channelId;
        this.messageId = messageId;
        this.guildId = guildId;
        this.emoji = emoji;
        this.burst = burst;
        this.type = type;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link User} who's reaction has been removed.
     *
     * @return The ID of the {@link User} who's reaction has been removed.
     */
    public Snowflake getUserId() {
        return Snowflake.of(userId);
    }

    /**
     * Requests to retrieve the {@link User} who's reaction has been removed.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} who's reaction has been removed.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser() {
        return getClient().getUserById(getUserId());
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link MessageChannel} containing the {@link Message} the reaction
     * was removed from.
     *
     * @return The ID of the {@link MessageChannel} involved.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Requests to retrieve the {@link MessageChannel} containing the {@link Message} the reaction was removed from.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel} containing the
     * {@link Message} involved. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<MessageChannel> getChannel() {
        return getClient().getChannelById(getChannelId()).cast(MessageChannel.class);
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Message} the reaction was removed from.
     *
     * @return The ID of the {@link Message} involved.
     */
    public Snowflake getMessageId() {
        return Snowflake.of(messageId);
    }

    /**
     * Requests to retrieve the {@link Message} the reaction was removed from.
     *
     * @return A {@link Mono} where, upon completion, emits the {@link Message} the reaction was removed from.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> getMessage() {
        return getClient().getMessageById(getChannelId(), getMessageId());
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} the {@link Message} involved is in, if present.
     * This may not be available if the {@code Message} was sent in a private channel.
     *
     * @return The ID of the {@link Guild} involved, if present.
     */
    public Optional<Snowflake> getGuildId() {
        return Optional.ofNullable(guildId).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the {@link Guild} the {@link Message} involved is in, if present.
     * This may not be available if the {@code Message} was sent in a private channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} containing the
     * {@link Message} involved, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(getClient()::getGuildById);
    }

    /**
     * The {@link ReactionEmoji} that was removed from a message.
     *
     * @return The {@code Emoji} that has been removed.
     */
    public ReactionEmoji getEmoji() {
        return emoji;
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
        return "ReactionRemoveEvent{" +
                "userId=" + userId +
                ", channelId=" + channelId +
                ", messageId=" + messageId +
                ", guildId=" + guildId +
                ", emoji=" + emoji +
                '}';
    }
}
