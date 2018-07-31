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

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Dispatched when all of the reactions on a message are removed.
 * <p>
 * {@link #guildId} may not be present if the message was in a private channel.
 * <p>
 * Corresponding {@link discord4j.core.event.domain.message.ReactionRemoveEvent reactions removes} are NOT dispatched
 * for messages included in this event.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#message-reaction-remove-all">Message Reaction
 * Remove All</a>
 */
public class ReactionRemoveAllEvent extends MessageEvent {

    private final long channelId;
    private final long messageId;
    @Nullable
    private final Long guildId;

    public ReactionRemoveAllEvent(DiscordClient client, long channelId, long messageId, @Nullable Long guildId) {
        super(client);
        this.channelId = channelId;
        this.messageId = messageId;
        this.guildId = guildId;
    }

    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    public Mono<MessageChannel> getChannel() {
        return getClient().getMessageChannelById(getChannelId());
    }

    public Snowflake getMessageId() {
        return Snowflake.of(messageId);
    }

    public Mono<Message> getMessage() {
        return getClient().getMessageById(getChannelId(), getMessageId());
    }

    public Optional<Snowflake> getGuildId() {
        return Optional.ofNullable(guildId).map(Snowflake::of);
    }

    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(getClient()::getGuildById);
    }
}
