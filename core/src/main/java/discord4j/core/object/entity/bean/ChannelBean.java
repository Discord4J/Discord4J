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
package discord4j.core.object.entity.bean;

import discord4j.common.json.response.ChannelResponse;

import java.io.Serializable;

public class ChannelBean implements Serializable {

    private static final long serialVersionUID = -8554782600684829226L;

    private long id;
    private int type;

    public ChannelBean(final ChannelResponse response) {
        id = response.getId();
        type = response.getType();
    }

    public ChannelBean() {}

    public final long getId() {
        return id;
    }

    public final void setId(final long id) {
        this.id = id;
    }

    public final int getType() {
        return type;
    }

    public final void setType(final int type) {
        this.type = type;
    }
}
