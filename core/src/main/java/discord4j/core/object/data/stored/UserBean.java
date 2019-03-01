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

import discord4j.common.json.UserResponse;
import reactor.util.annotation.Nullable;

import java.io.Serializable;

public final class UserBean implements Serializable {

    private static final long serialVersionUID = 1555537329840118514L;

    private long id;
    private String username;
    private String discriminator;
    @Nullable
    private String avatar;
    private boolean isBot;

    public UserBean(final UserResponse response) {
        id = response.getId();
        username = response.getUsername();
        discriminator = response.getDiscriminator();
        avatar = response.getAvatar();
        isBot = response.isBot() != null && response.isBot();
    }

    public UserBean(final UserBean toCopy) {
        id = toCopy.id;
        username = toCopy.username;
        discriminator = toCopy.discriminator;
        avatar = toCopy.avatar;
        isBot = toCopy.isBot;
    }

    public UserBean() {}

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(final String discriminator) {
        this.discriminator = discriminator;
    }

    @Nullable
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(@Nullable final String avatar) {
        this.avatar = avatar;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setBot(boolean bot) {
        isBot = bot;
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", discriminator='" + discriminator + '\'' +
                ", avatar='" + avatar + '\'' +
                ", isBot=" + isBot +
                '}';
    }
}
