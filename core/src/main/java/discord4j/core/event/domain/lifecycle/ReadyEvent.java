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

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;

import java.util.Objects;
import java.util.Set;

/**
 * Dispatched when an initial connection to the Discord gateway has been established.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#ready">Ready</a>
 */
public class ReadyEvent extends GatewayLifecycleEvent {

    private final int gatewayVersion;
    private final User self;
    private final Set<Guild> guilds;
    private final String sessionId;
    private final String[] trace;

    public ReadyEvent(DiscordClient client, int gatewayVersion, User self, Set<Guild> guilds, String sessionId,
                      String[] trace) {
        super(client);
        this.gatewayVersion = gatewayVersion;
        this.self = self;
        this.guilds = guilds;
        this.sessionId = sessionId;
        this.trace = trace;
    }

    public int getGatewayVersion() {
        return gatewayVersion;
    }

    public User getSelf() {
        return self;
    }

    public Set<Guild> getGuilds() {
        return guilds;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String[] getTrace() {
        return trace;
    }

    public static class Guild {

        private final long id;
        private final boolean available;

        public Guild(long id, boolean available) {
            this.id = id;
            this.available = available;
        }

        public Snowflake getId() {
            return Snowflake.of(id);
        }

        public boolean isAvailable() {
            return available;
        }

        @Override
        public boolean equals(Object o) {
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
    }

}
