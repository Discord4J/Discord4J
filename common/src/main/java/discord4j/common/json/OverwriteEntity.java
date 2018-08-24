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
package discord4j.common.json;

import discord4j.common.jackson.UnsignedJson;

public class OverwriteEntity {

    @UnsignedJson
    private long id;
    private String type;
    private long allow;
    private long deny;

    public OverwriteEntity(long id, String type, long allow, long deny) {
        this.id = id;
        this.type = type;
        this.allow = allow;
        this.deny = deny;
    }

    public OverwriteEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getAllow() {
        return allow;
    }

    public void setAllow(long allow) {
        this.allow = allow;
    }

    public long getDeny() {
        return deny;
    }

    public void setDeny(long deny) {
        this.deny = deny;
    }

    @Override
    public String toString() {
        return "OverwriteEntity{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", allow=" + allow +
                ", deny=" + deny +
                '}';
    }
}
