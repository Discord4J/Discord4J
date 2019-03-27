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
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when a message is deleted.
 * <p>
 * The deleted message may not be present if messages are not stored.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#message-delete">Message Delete</a>
 */
public class MessageDeleteEvent extends MessageEvent {

    private final long messageId;
    private final long channelId;
    @Nullable
    private final Message message;

    public MessageDeleteEvent(DiscordClient client, long messageId, long channelId, @Nullable Message message) {
        super(client);
        this.messageId = messageId;
        this.channelId = channelId;
        this.message = message;
    }

    /**
     * Gets the Snowflake ID of the message that was deleted.
     *
     * @return The ID of the deleted message.
     */
    public Snowflake getMessageId() {
        return Snowflake.of(messageId);
    }

    /**
     * Gets the Message that was deleted in this event, if present. This may not be available if messages are not stored.
     *
     * @return The deleted message, if present.
     */
    public Optional<Message> getMessage() {
        return Optional.ofNullable(message);
    }

    /**
     * Gets the Snowflake ID of the channel the message was deleted from.
     *
     * @return The ID of the channel that the message was deleted from.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Requests to retrieve the MessageChannel the Message was deleted from.
     *
     * @return A {@link Mono} where, upon successful completion, emits the MessageChannel the message was deleted in. If an error is received, it is emitted through the Mono.
     */
    public Mono<MessageChannel> getChannel() {
        return getClient().getChannelById(getChannelId()).cast(MessageChannel.class);
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

