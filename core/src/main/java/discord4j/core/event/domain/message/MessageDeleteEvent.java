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

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Dispatched when a message is deleted.
 * <p>
 * The deleted message may not be present if messages are not stored.
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

    public Snowflake getMessageId() {
        return Snowflake.of(messageId);
    }

    public Optional<Message> getMessage() {
        return Optional.ofNullable(message);
    }

    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    public Mono<MessageChannel> getChannel() {
        return getClient().getMessageChannelById(getChannelId());
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

