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

import discord4j.common.json.OverwriteEntity;

public class PermissionOverwriteData {

    private final long id;
    private final String type;
    private final long allow;
    private final long deny;

    public PermissionOverwriteData(OverwriteEntity response) {
        id = response.getId();
        type = response.getType();
        allow = response.getAllow();
        deny = response.getDeny();
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public long getAllow() {
        return allow;
    }

    public long getDeny() {
        return deny;
    }

    @Override
    public String toString() {
        return "PermissionOverwriteData{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", allow=" + allow +
                ", deny=" + deny +
                '}';
    }
}
