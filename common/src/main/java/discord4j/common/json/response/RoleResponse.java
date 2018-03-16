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
package discord4j.common.json.response;

import discord4j.common.jackson.UnsignedJson;

public class RoleResponse {

    @UnsignedJson
    private long id;
    private String name;
    private int color;
    private boolean hoist;
    private int position;
    private int permissions;
    private boolean managed;
    private boolean mentionable;

    public long getId() {
        return id;
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

    public int getPosition() {
        return position;
    }

    public int getPermissions() {
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
        return "RoleResponse[" +
                "id=" + id +
                ", name=" + name +
                ", color=" + color +
                ", hoist=" + hoist +
                ", position=" + position +
                ", permissions=" + permissions +
                ", managed=" + managed +
                ", mentionable=" + mentionable +
                ']';
    }
}
