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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.common.util.Snowflake;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Dispatched when a role is deleted in a guild.
 * <p>
 * The deleted role may not be present if roles are not stored.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#guild-role-delete">Guild Role Delete</a>
 */
public class RoleDeleteEvent extends RoleEvent {

    private final long guildId;
    private final long roleId;
    @Nullable
    private final Role role;

    public RoleDeleteEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long guildId, long roleId, @Nullable Role role) {
        super(gateway, shardInfo);
        this.guildId = guildId;
        this.roleId = roleId;
        this.role = role;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} the {@link Role} was deleted in.
     *
     * @return The ID of the {@link Guild} involved.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the {@link Guild} the {@link Role} was deleted in.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} containing the deleted
     * {@link Role}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Role} that was deleted in this event.
     *
     * @return The ID of the deleted {@link Role}.
     *
     */
    public Snowflake getRoleId() {
        return Snowflake.of(roleId);
    }

    /**
     * Gets the {@link Role} that was deleted in this event, if present. This may not be available if {@code Roles} are
     * not stored.
     *
     * @return The {@link Role} that was deleted in this event, if present.
     */
    public Optional<Role> getRole() {
        return Optional.ofNullable(role);
    }

    @Override
    public String toString() {
        return "RoleDeleteEvent{" +
                "guildId=" + guildId +
                ", roleId=" + roleId +
                ", role=" + role +
                '}';
    }
}
