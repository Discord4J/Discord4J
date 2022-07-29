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
package discord4j.core.event.domain.lifecycle;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.gateway.Ready;
import discord4j.gateway.ShardInfo;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Dispatched when an initial connection to the Discord gateway has been established.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#ready">Ready</a>
 */
public class ReadyEvent extends GatewayLifecycleEvent {

    private final Ready data;
    private final User self;
    private final Set<Guild> guilds;

    public ReadyEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Ready data, User self, Set<Guild> guilds) {
        super(gateway, shardInfo);
        this.data = data;
        this.self = self;
        this.guilds = guilds;
    }

    public Ready getData() {
        return data;
    }

    /**
     * Gets the gateway protocol version being used. Ex. 6.
     *
     * @return The gateway protocol version being used.
     */
    public int getGatewayVersion() {
        return data.v();
    }

    /**
     * Gets the bot {@link User}.
     *
     * @return The bot {@link User}.
     */
    public User getSelf() {
        return self;
    }

    /**
     * Gets a set of Unavailable {@link Guild}. These {@code Guilds} have not yet been provided via a
     * {@link discord4j.core.event.domain.guild.GuildCreateEvent}
     *
     * @return A set of unavailable {@code Guilds}.
     * @see <a href="https://discord.com/developers/docs/resources/guild#unavailable-guild-object">
     * Unavailable Guild Object</a>
     */
    public Set<Guild> getGuilds() {
        return guilds;
    }

    /**
     * Gets the current session ID of the connection.
     *
     * @return the session ID of the connection
     */
    public String getSessionId() {
        return data.sessionId();
    }

    /**
     * Gets the trace provided by Discord. Used for debugging - The {@code Guilds} the user is in.
     *
     * @return The trace provided by Discord.
     */
    public /*~~>*/List<String> getTrace() {
        return data.trace();
    }

    /**
     * An incomplete Guild provided by Discord upon the ready event.
     */
    public static class Guild {

        private final long id;
        private final boolean available;

        public Guild(long id, boolean available) {
            this.id = id;
            this.available = available;
        }

        /**
         * Gets the {@link Snowflake} ID of the guild.
         *
         * @return the {@link Snowflake} ID of the guild.
         */
        public Snowflake getId() {
            return Snowflake.of(id);
        }

        /**
         * Whether or not the Guild has been made available via a
         * {@link discord4j.core.event.domain.guild.GuildCreateEvent}
         *
         * @return Whether or not the Guild has been made available yet.
         */
        public boolean isAvailable() {
            return available;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Guild guild = (Guild) o;
            return id == guild.id &&
                    available == guild.available;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, available);
        }

        @Override
        public String toString() {
            return "Guild{" +
                    "id=" + id +
                    ", available=" + available +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ReadyEvent{" +
                "data=" + data +
                ", self=" + self +
                ", guilds=" + guilds +
                '}';
    }
}
