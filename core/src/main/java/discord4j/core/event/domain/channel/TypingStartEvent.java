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
package discord4j.core.event.domain.channel;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.user.UserEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Dispatched when a user starts typing in a message channel.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#typing-start">Typing Start</a>
 */
public class TypingStartEvent extends AbstractChannelEvent implements UserEvent {

    private final long userId;
    private final Instant startTime;

    public TypingStartEvent(DiscordClient client, long channelId, long userId, Instant startTime) {
        super(client, channelId);
        this.userId = userId;
        this.startTime = startTime;
    }

    public Mono<MessageChannel> getChannel() {
        return getClient().getChannelById(getChannelId()).cast(MessageChannel.class);
    }

    @Override
    public Snowflake getUserId() {
        return Snowflake.of(userId);
    }

    public Mono<User> getUser() {
        return getClient().getUserById(getUserId());
    }

    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return "TypingStartEvent{" +
                "userId=" + userId +
                ", startTime=" + startTime +
                '}';
    }
}
