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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dispatched when multiple messages are deleted at once.
 * <p>
 * Corresponding {@link discord4j.core.event.domain.message.MessageDeleteEvent message deletes} are NOT dispatched for
 * messages included in this event.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#message-delete-bulk">Message Delete Bulk</a>
 */
public class MessageBulkDeleteEvent extends MessageEvent {

    private final List<Long> messageIds;
    private final long channelId;
    private final long guildId;
    private final Set<Message> messages;

    public MessageBulkDeleteEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, List<Long> messageIds, long channelId, long guildId,
                                  Set<Message> messages) {
        super(gateway, shardInfo);
        this.messageIds = messageIds;
        this.channelId = channelId;
        this.guildId = guildId;
        this.messages = messages;
    }

    /**
     * Gets a list of {@link Snowflake} IDs of the messages that were deleted.
     *
     * @return a list of IDs of the messages that were deleted.
     */
    public Set<Snowflake> getMessageIds() {
        return messageIds.stream()
                .map(Snowflake::of)
                .collect(Collectors.toSet());
    }

    /**
     * Gets a list of {@link Message} objects there were deleted in this event.
     *
     * @return a list of {@link Message} objects that were deleted.
     */
    public Set<Message> getMessages() {
        return messages;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link MessageChannel} the messages were deleted in.
     *
     * @return The ID of the {@link MessageChannel} that the messages were deleted in.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Requests to retrieve the {@link MessageChannel} representation of the {@code Channel} the messages were deleted
     * in.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel} the messages
     * were deleted from. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<MessageChannel> getChannel() {
        return getClient().getChannelById(getChannelId()).cast(MessageChannel.class);
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} the messages were deleted in.
     *
     * @return The ID of the {@link Guild} the messages were deleted in.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the {@link Guild} the messages were deleted in.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} the messages
     * where deleted from. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    @Override
    public String toString() {
        return "MessageBulkDeleteEvent{" +
                "messageIds=" + messageIds +
                ", channelId=" + channelId +
                ", guildId=" + guildId +
                ", messages=" + messages +
                '}';
    }
}
