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
package discord4j.core.event.domain.guild.role;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when a role is deleted in a guild.
 * <p>
 * The deleted role may not be present if roles are not stored.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#guild-role-delete">Guild Role Delete</a>
 */
public class RoleDeleteEvent extends AbstractRoleEvent {

    @Nullable
    private Role role;

    public RoleDeleteEvent(DiscordClient client, long guildId, long roleId, @Nullable Role role) {
        super(client, guildId, roleId);
        this.role = role;
    }

    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    public Optional<Role> getRole() {
        return Optional.ofNullable(role);
    }

    @Override
    public String toString() {
        return "RoleDeleteEvent{" +
                "role=" + role +
                '}';
    }
}
