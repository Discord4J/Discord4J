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
package discord4j.core.object.data.stored;

import discord4j.common.json.RoleResponse;

import java.io.Serializable;

public final class RoleBean implements Serializable {

    private static final long serialVersionUID = 5330465140581253151L;

    private long id;
    private int position;
    private String name;
    private int color;
    private boolean hoist;
    private long permissions;
    private boolean managed;
    private boolean mentionable;

    public RoleBean(final RoleResponse response) {
        id = response.getId();
        position = response.getPosition();
        name = response.getName();
        color = response.getColor();
        hoist = response.isHoist();
        permissions = response.getPermissions();
        managed = response.isManaged();
        mentionable = response.isMentionable();
    }

    public RoleBean() {}

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(final int position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(final int color) {
        this.color = color;
    }

    public boolean isHoist() {
        return hoist;
    }

    public void setHoist(final boolean hoist) {
        this.hoist = hoist;
    }

    public long getPermissions() {
        return permissions;
    }

    public void setPermissions(final long permissions) {
        this.permissions = permissions;
    }

    public boolean isManaged() {
        return managed;
    }

    public void setManaged(final boolean managed) {
        this.managed = managed;
    }

    public boolean isMentionable() {
        return mentionable;
    }

    public void setMentionable(final boolean mentionable) {
        this.mentionable = mentionable;
    }

    @Override
    public String toString() {
        return "RoleBean{" +
                "id=" + id +
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
