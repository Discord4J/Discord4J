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
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Dispatched when a message is deleted.
 * <p>
 * The deleted message may not be present if messages are not stored.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#message-delete">Message Delete</a>
 */
public class MessageDeleteEvent extends MessageEvent {

    private final long messageId;
    private final long channelId;
    @Nullable
    private final Long guildId;
    @Nullable
    private final Message message;

    public MessageDeleteEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long messageId, long channelId, @Nullable Long guildId, @Nullable Message message) {
        super(gateway, shardInfo);
        this.messageId = messageId;
        this.channelId = channelId;
        this.guildId = guildId;
        this.message = message;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Message} that was deleted.
     *
     * @return The ID of the deleted {@link Message}.
     */
    public Snowflake getMessageId() {
        return Snowflake.of(messageId);
    }

    /**
     * Gets the {@link Message} that was deleted in this event, if present.
     * This may not be available if {@code Messages} are not stored.
     *
     * @return The deleted {@link Message}, if present.
     */
    public Optional<Message> getMessage() {
        return Optional.ofNullable(message);
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link MessageChannel} the {@link Message} was deleted from.
     *
     * @return The ID of the {@link MessageChannel} that the {@link Message} was deleted from.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Requests to retrieve the {@link MessageChannel} the {@link Message} was deleted from.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel} the
     * {@link Message} was deleted in. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<MessageChannel> getChannel() {
        return getClient().getChannelById(getChannelId()).cast(MessageChannel.class);
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} the
     * {@link discord4j.core.object.entity.Message} was deleted from, if present.
     * This may not be available if the deleted {@code Message} was from a private channel.
     *
     * @return The ID of the {@link Guild} involved, if present.
     */
    public Optional<Snowflake> getGuildId() {
        return Optional.ofNullable(guildId).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the {@link Guild} the
     * {@link discord4j.core.object.entity.Message} was deleted from, if present.
     * This may not be available if the deleted {@code Message} was from a private channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} that contained the
     * {@link Message} involved, if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(getClient()::getGuildById);
    }

    @Override
    public String toString() {
        return "MessageDeleteEvent{" +
                "messageId=" + messageId +
                ", channelId=" + channelId +
                ", message=" + message +
                '}';
    }
}

