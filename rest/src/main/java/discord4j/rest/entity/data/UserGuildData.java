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

import discord4j.rest.json.response.UserGuildResponse;

public class UserGuildData {

    private final long id;
    private final String name;
    private final String icon;
    private final boolean owner;
    private final long permissions;

    public UserGuildData(UserGuildResponse response) {
        id = response.getId();
        name = response.getName();
        icon = response.getIcon();
        owner = response.isOwner();
        permissions = response.getPermissions();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isOwner() {
        return owner;
    }

    public long getPermissions() {
        return permissions;
    }

    @Override
    public String toString() {
        return "UserGuildData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", owner=" + owner +
                ", permissions=" + permissions +
                '}';
    }
}
