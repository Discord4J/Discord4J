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
import discord4j.core.event.Update;
import discord4j.core.object.PermissionSet;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Optional;

public class RoleUpdateEvent extends RoleEvent {

    private final Update<String> name;
    private final Update<Color> color;
    private final Update<Boolean> hoist;
    private final Update<Integer> position;
    private final Update<PermissionSet> permissions;
    private final Update<Boolean> mentionable;

    public RoleUpdateEvent(DiscordClient client, @Nullable Update<String> name, @Nullable Update<Color> color,
                           @Nullable Update<Boolean> hoist, @Nullable Update<Integer> position,
                           @Nullable Update<PermissionSet> permissions, @Nullable Update<Boolean> mentionable) {
        super(client);
        this.name = name;
        this.color = color;
        this.hoist = hoist;
        this.position = position;
        this.permissions = permissions;
        this.mentionable = mentionable;
    }

    public Optional<Update<String>> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<Update<Color>> getColor() {
        return Optional.ofNullable(color);
    }

    public Optional<Update<Boolean>> isHoisted() {
        return Optional.ofNullable(hoist);
    }

    public Optional<Update<Integer>> getPosition() {
        return Optional.ofNullable(position);
    }

    public Optional<Update<PermissionSet>> getPermissions() {
        return Optional.ofNullable(permissions);
    }

    public Optional<Update<Boolean>> isMentionable() {
        return Optional.ofNullable(mentionable);
    }
}
