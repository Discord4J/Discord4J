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

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Dispatched when a role is deleted in a guild.
 * <p>
 * The deleted role may not be present if roles are not stored.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#guild-role-delete">Guild Role Delete</a>
 */
public class RoleDeleteEvent extends RoleEvent {

    private final long guildId;
    private final long roleId;
    @Nullable
    private Role role;

    public RoleDeleteEvent(DiscordClient client, long guildId, long roleId, @Nullable Role role) {
        super(client);
        this.guildId = guildId;
        this.roleId = roleId;
        this.role = role;
    }

    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    public Snowflake getRoleId() {
        return Snowflake.of(roleId);
    }

    public Optional<Role> getRole() {
        return Optional.ofNullable(role);
    }
}
