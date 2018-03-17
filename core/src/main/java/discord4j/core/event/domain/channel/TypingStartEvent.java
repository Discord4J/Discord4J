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

import discord4j.core.event.domain.Event;
import discord4j.core.object.Snowflake;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

import java.time.Instant;

public class TypingStartEvent implements Event {

    private final long channelId;
    private final long userId;
    private final Instant startTime;

    public TypingStartEvent(long channelId, long userId, Instant startTime) {
        this.channelId = channelId;
        this.userId = userId;
        this.startTime = startTime;
    }

    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    public Mono<MessageChannel> getChannel() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    public Snowflake getUserId() {
        return Snowflake.of(userId);
    }

    public Mono<User> getUser() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    public Instant getStartTime() {
        return startTime;
    }
}
