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
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dispatched when multiple messages are deleted at once.
 * <p>
 * Corresponding {@link discord4j.core.event.domain.message.MessageDeleteEvent message deletes} are NOT dispatched for
 * messages included in this event.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#message-delete-bulk">Message Delete Bulk</a>
 */
public class MessageBulkDeleteEvent extends MessageEvent {

    private final long[] messageIds;
    private final long channelId;
    private final long guildId;
    private final Set<Message> messages;

    public MessageBulkDeleteEvent(DiscordClient client, long[] messageIds, long channelId, long guildId,
                                  Set<Message> messages) {
        super(client);
        this.messageIds = messageIds;
        this.channelId = channelId;
        this.guildId = guildId;
        this.messages = messages;
    }

    public Set<Snowflake> getMessageIds() {
        return Arrays.stream(messageIds)
                .mapToObj(Snowflake::of)
                .collect(Collectors.toSet());
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    public Mono<MessageChannel> getChannel() {
        return getClient().getMessageChannelById(getChannelId());
    }

    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }
}
