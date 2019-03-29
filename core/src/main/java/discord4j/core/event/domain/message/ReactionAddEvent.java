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

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when a reaction is added to a message.
 * <p>
 * {@link #guildId} may not be present if the message was in a private channel.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#message-reaction-add">Message Reaction Add</a>
 */
public class ReactionAddEvent extends MessageEvent {

    private final long userId;
    private final long channelId;
    private final long messageId;
    @Nullable
    private final Long guildId;
    private final ReactionEmoji emoji;

    public ReactionAddEvent(DiscordClient client, long userId, long channelId, long messageId, @Nullable Long guildId,
                            ReactionEmoji emoji) {
        super(client);
        this.userId = userId;
        this.channelId = channelId;
        this.messageId = messageId;
        this.guildId = guildId;
        this.emoji = emoji;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link User} who added a reaction in this event.
     *
     * @return The Id of the {@link User} who added a reaction.
     */
    public Snowflake getUserId() {
        return Snowflake.of(userId);
    }

    /**
     * Requests to retrieve the {@link User} who added a reaction in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} that has added the reaction.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser() {
        return getClient().getUserById(getUserId());
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link MessageChannel} the {@link Message} and reaction are in.
     *
     * @return The ID of the {@link MessageChannel} involved.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Requests to retrieve the {@link MessageChannel} the {@link Message} and reaction are in.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel} containing
     * the {@link Message} in the event. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<MessageChannel> getChannel() {
        return getClient().getChannelById(getChannelId()).cast(MessageChannel.class);
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Message} the reaction was added to in this event.
     *
     * @return The ID of the {@link Message} the reaction was added to.
     */
    public Snowflake getMessageId() {
        return Snowflake.of(messageId);
    }

    /**
     * Request to retrieve the {@link Message} the reaction was added to in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} the reaction was added to.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> getMessage() {
        return getClient().getMessageById(getChannelId(), getMessageId());
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} containing the {@link Message} and Reaction, if present.
     * This may not be available if the reaction is to a {@code Message} in a private channel.
     *
     * @return The ID of the {@link Guild} involved in the event, if present.
     */
    public Optional<Snowflake> getGuildId() {
        return Optional.ofNullable(guildId).map(Snowflake::of);
    }

    /**
     * Request to retrieve the {@link Guild} containing the {@link Message} and reaction, if present.
     * This may not be available if the reaction is to a {@code Message} in a private channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} containing the {@link Message}
     * involved, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(getClient()::getGuildById);
    }

    /**
     * Gets the {@link ReactionEmoji} that was added to the {@link Message} in this event.
     *
     * @return The {@code Emoji} added to the {@link Message} as a reaction.
     */
    public ReactionEmoji getEmoji() {
        return emoji;
    }

    @Override
    public String toString() {
        return "ReactionAddEvent{" +
                "userId=" + userId +
                ", channelId=" + channelId +
                ", messageId=" + messageId +
                ", guildId=" + guildId +
                ", emoji=" + emoji +
                '}';
    }
}
