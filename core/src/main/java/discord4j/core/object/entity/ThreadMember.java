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
package discord4j.core.object.entity;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.ThreadMemberData;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * A thread member is used to indicate whether a user has joined a thread or not.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#thread-member-object">Thread Member Object</a>
 */
@Experimental
public final class ThreadMember {

    private final GatewayDiscordClient gateway;
    private final ThreadMemberData data;

    /**
     * Constructs a {@code ThreadMember} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ThreadMember(final GatewayDiscordClient gateway, final ThreadMemberData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    public Snowflake getThreadId() {
        return Snowflake.of(data.id());
    }

    public Snowflake getUserId() {
        // TODO: handle guild_create thread member objects, meaning threads visible by self
        return Snowflake.of(data.userId());
    }

    public Instant getJoinTimestamp() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(data.joinTimestamp(), Instant::from);
    }

    public int getFlags() {
        return data.flags();
    }

    @Override
    public String toString() {
        return "ThreadMember{" +
                "gateway=" + gateway +
                ", data=" + data +
                '}';
    }
}
