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
import discord4j.core.object.entity.Role;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Dispatched when a role is updated in a guild.
 * <p>
 * The old role may not be present if roles are not stored.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#guild-role-update">Guild Role Update</a>
 */
public class RoleUpdateEvent extends RoleEvent {

    private final Role current;
    @Nullable
    private final Role old;

    public RoleUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Role current, @Nullable Role old) {
        super(gateway, shardInfo);
        this.current = current;
        this.old = old;
    }

    /**
     * Gets the current, new version of the {@link Role} that was updated in the event.
     *
     * @return The current version of the updated {@link Role}.
     */
    public Role getCurrent() {
        return current;
    }

    /**
     * Gets the old version of the {@link Role} that was updated in this event. if present.
     * This may not be available if {@code Role} are not stored.
     *
     * @return The old version of the updated {@link Role}, if present.
     */
    public Optional<Role> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public String toString() {
        return "RoleUpdateEvent{" +
                "current=" + current +
                ", old=" + old +
                '}';
    }
}
