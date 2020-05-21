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
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.common.util.Snowflake;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when all of the reactions on a message are removed.
 * <p>
 * {@link #guildId} may not be present if the message was in a private channel.
 * <p>
 * Corresponding {@link discord4j.core.event.domain.message.ReactionRemoveEvent reactions removes} are NOT dispatched
 * for messages included in this event.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#message-reaction-remove-all">Message Reaction
 * Remove All</a>
 */
public class ReactionRemoveAllEvent extends MessageEvent {

    private final long channelId;
    private final long messageId;
    @Nullable
    private final Long guildId;

    public ReactionRemoveAllEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long channelId, long messageId, @Nullable Long guildId) {
        super(gateway, shardInfo);
        this.channelId = channelId;
        this.messageId = messageId;
        this.guildId = guildId;
    }

    /**
     * Gets the {@link Snowflake} ID of the channel containing the {@link Message} and the removed Reactions.
     *
     * @return The ID of the {@link MessageChannel} involved.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Requests to retrieve the {@link MessageChannel} containing the {@link Message} and the removed reactions.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel} containing the message
     * involved. If an error is received, it is emitted through the {@code Mono}
     */
    public Mono<MessageChannel> getChannel() {
        return getClient().getChannelById(getChannelId()).cast(MessageChannel.class);
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Message} the reactions were removed from in this event.
     *
     * @return The ID of the {@link Message} involved.
     */
    public Snowflake getMessageId() {
        return Snowflake.of(messageId);
    }

    /**
     * Requests to retrieve the {@link Message} the reactions were removed from in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} the reactions were
     * removed from. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> getMessage() {
        return getClient().getMessageById(getChannelId(), getMessageId());
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} containing the {@link Message} the
     * reactions were removed from, if present.
     * This may not be available if the {@code Message} was sent in a private channel.
     *
     * @return The ID of the {@link Guild} containing the {@link Message} involved, if present.
     */
    public Optional<Snowflake> getGuildId() {
        return Optional.ofNullable(guildId).map(Snowflake::of);
    }

    /**
     * Request to retrieve the {@link Guild} containing the {@link Message} the reactions were removed from, if present.
     * This may not if the {@code Message} was sent in a private channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} containing
     * the {@link Message} involved, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(getClient()::getGuildById);
    }

    @Override
    public String toString() {
        return "ReactionRemoveAllEvent{" +
                "channelId=" + channelId +
                ", messageId=" + messageId +
                ", guildId=" + guildId +
                '}';
    }
}
