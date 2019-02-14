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
import discord4j.core.object.entity.Role;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when a role is updated in a guild.
 * <p>
 * The old role may not be present if messages are not stored.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#guild-role-update">Guild Role Update</a>
 */
public class RoleUpdateEvent extends RoleEvent {

    private final Role current;
    private final Role old;

    public RoleUpdateEvent(DiscordClient client, Role current, @Nullable Role old) {
        super(client);
        this.current = current;
        this.old = old;
    }

    public Role getCurrent() {
        return current;
    }

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
