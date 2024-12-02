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

    /**
     * Gets the data of the thread member.
     *
     * @return The data of the thread member.
     */
    public ThreadMemberData getData() {
        return data;
    }

    /**
     * Gets the ID of thread which member is associated.
     *
     * @return The ID of thread channel.
     */
    public Snowflake getThreadId() {
        // ThreadMemberData#id is absent in GUILD_CREATE (threads self is part of)
        // TODO D4J should be able to include the missing value with the outer thread id when creating ThreadMember
        return Snowflake.of(data.id().toOptional().orElseThrow(IllegalStateException::new));
    }

    /**
     * Gets the ID of user.
     *
     * @return The ID of user.
     */
    public Snowflake getUserId() {
        // ThreadMemberData#userId is absent in GUILD_CREATE (threads self is part of)
        // TODO D4J should be able to include the missing value with the self id when creating ThreadMember
        return Snowflake.of(data.userId().toOptional().orElseThrow(IllegalStateException::new));
    }

    /**
     * Gets when the user joined the thread.
     *
     * @return When the user joined the thread.
     */
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
