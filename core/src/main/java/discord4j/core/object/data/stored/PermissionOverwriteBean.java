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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import discord4j.common.json.OverwriteEntity;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public final class PermissionOverwriteBean implements Serializable {

    private static final long serialVersionUID = 6913011818425161574L;

    private long id;
    private String type;
    private long allow;
    private long deny;

    public PermissionOverwriteBean(final OverwriteEntity response) {
        id = response.getId();
        type = response.getType();
        allow = response.getAllow();
        deny = response.getDeny();
    }

    public PermissionOverwriteBean() {}

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public long getAllow() {
        return allow;
    }

    public void setAllow(final long allow) {
        this.allow = allow;
    }

    public long getDeny() {
        return deny;
    }

    public void setDeny(final long deny) {
        this.deny = deny;
    }

    @Override
    public String toString() {
        return "PermissionOverwriteBean{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", allow=" + allow +
                ", deny=" + deny +
                '}';
    }
}
