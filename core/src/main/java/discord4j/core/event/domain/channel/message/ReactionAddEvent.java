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
package discord4j.core.event.domain.channel.message;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.user.UserEvent;
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
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#message-reaction-add">Message Reaction Add</a>
 */
public class ReactionAddEvent extends AbstractMessageEvent implements UserEvent {

    private final long userId;
    @Nullable
    private final Long guildId;
    private final ReactionEmoji emoji;

    public ReactionAddEvent(DiscordClient client, long userId, long channelId, long messageId, @Nullable Long guildId,
                            ReactionEmoji emoji) {
        super(client, channelId, messageId);
        this.userId = userId;
        this.guildId = guildId;
        this.emoji = emoji;
    }

    @Override
    public Snowflake getUserId() {
        return Snowflake.of(userId);
    }

    public Mono<User> getUser() {
        return getClient().getUserById(getUserId());
    }

    public Mono<MessageChannel> getChannel() {
        return getClient().getChannelById(getChannelId()).cast(MessageChannel.class);
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

    public ReactionEmoji getEmoji() {
        return emoji;
    }

    @Override
    public String toString() {
        return "ReactionAddEvent{" +
                "userId=" + userId +
                "guildId=" + guildId +
                ", emoji=" + emoji +
                '}';
    }
}
