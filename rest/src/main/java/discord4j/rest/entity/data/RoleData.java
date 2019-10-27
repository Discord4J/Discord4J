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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.rest.entity.data;

import discord4j.common.json.RoleResponse;

public class RoleData {

    private final long guildId;
    private final long id;
    private final int position;
    private final String name;
    private final int color;
    private final boolean hoist;
    private final long permissions;
    private final boolean managed;
    private final boolean mentionable;

    public RoleData(long guildId, RoleResponse response) {
        this.guildId = guildId;
        this.id = response.getId();
        this.position = response.getPosition();
        this.name = response.getName();
        this.color = response.getColor();
        this.hoist = response.isHoist();
        this.permissions = response.getPermissions();
        this.managed = response.isManaged();
        this.mentionable = response.isMentionable();
    }

    public long getGuildId() {
        return guildId;
    }

    public long getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    public boolean isHoist() {
        return hoist;
    }

    public long getPermissions() {
        return permissions;
    }

    public boolean isManaged() {
        return managed;
    }

    public boolean isMentionable() {
        return mentionable;
    }

    @Override
    public String toString() {
        return "RoleData{" +
                "guildId=" + guildId +
                ", id=" + id +
                ", position=" + position +
                ", name='" + name + '\'' +
                ", color=" + color +
                ", hoist=" + hoist +
                ", permissions=" + permissions +
                ", managed=" + managed +
                ", mentionable=" + mentionable +
                '}';
    }
}
