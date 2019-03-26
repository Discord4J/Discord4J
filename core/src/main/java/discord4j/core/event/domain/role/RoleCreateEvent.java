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
package discord4j.core.event.domain.role;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Dispatched when a role is created in a guild.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#guild-role-create">Guild Role Create</a>
 */
public class RoleCreateEvent extends RoleEvent {

    private final long guildId;
    private final Role role;

    public RoleCreateEvent(DiscordClient client, long guildId, Role role) {
        super(client);
        this.guildId = guildId;
        this.role = role;
    }

    /**
     * Gets the Snowflake ID of the Guild the role was created in.
     * @return The ID of the Guild the role was created in.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Gets the Guild the role was created in.
     * @return The Guild involved.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the role that was created in this event.
     * @return The Role that was created.
     */
    public Role getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "RoleCreateEvent{" +
                "guildId=" + guildId +
                ", role=" + role +
                '}';
    }
}
