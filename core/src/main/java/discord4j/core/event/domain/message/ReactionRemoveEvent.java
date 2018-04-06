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
import discord4j.core.object.Snowflake;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

public class ReactionRemoveEvent extends MessageEvent {

    private final long userId;
    private final long channelId;
    private final long messageId;
    private final GuildEmoji emoji; // TODO need GuildEmoji | Unicode type

    public ReactionRemoveEvent(DiscordClient client, long userId, long channelId, long messageId, GuildEmoji emoji) {
        super(client);
        this.userId = userId;
        this.channelId = channelId;
        this.messageId = messageId;
        this.emoji = emoji;
    }

    public Snowflake getUserId() {
        return Snowflake.of(userId);
    }

    public Mono<User> getUser() {
        return getClient().getUserById(getUserId());
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

    public GuildEmoji getEmoji() {
        return emoji;
    }

}
