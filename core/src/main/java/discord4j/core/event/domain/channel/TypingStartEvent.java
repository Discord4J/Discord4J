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
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Dispatched when a user starts typing in a message channel.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#typing-start">Typing Start</a>
 */
public class TypingStartEvent extends ChannelEvent {

    private final long channelId;
    private final long userId;
    private final Instant startTime;

    public TypingStartEvent(DiscordClient client, long channelId, long userId, Instant startTime) {
        super(client);
        this.channelId = channelId;
        this.userId = userId;
        this.startTime = startTime;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link MessageChannel} the user has started typing in.
     *
     * @return the ID of the {@link MessageChannel} the {@link User} is typing in.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Requests to retrieve the {@link MessageChannel} the user has started typing in.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel} the {@link User} has
     * started typing in. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<MessageChannel> getChannel() {
        return getClient().getChannelById(getChannelId()).cast(MessageChannel.class);
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link User} who has started typing in this event.
     *
     * @return The ID of the {@link User} who has started typing.
     */
    public Snowflake getUserId() {
        return Snowflake.of(userId);
    }

    /**
     * Requests to retrieve the {@link User} who has started typing in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} that has started typing.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser() {
        return getClient().getUserById(getUserId());
    }

    /**
     * Gets the time at which the {@link User} started typing in this event.
     *
     * @return The time at which the {@link User} starting typing.
     */
    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return "TypingStartEvent{" +
                "channelId=" + channelId +
                ", userId=" + userId +
                ", startTime=" + startTime +
                '}';
    }
}
