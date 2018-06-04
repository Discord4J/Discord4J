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
package discord4j.core.object.data;

import discord4j.core.object.data.stored.UserBean;
import discord4j.rest.json.response.BanResponse;

import javax.annotation.Nullable;
import java.io.Serializable;

public final class BanBean implements Serializable {

    @Nullable
    private String reason;
    private UserBean user;

    public BanBean(final BanResponse response) {
        this.reason = response.getReason();
        this.user = new UserBean(response.getUser());
    }

    public BanBean() {}

    @Nullable
    public String getReason() {
        return reason;
    }

    public UserBean getUser() {
        return user;
    }
}
