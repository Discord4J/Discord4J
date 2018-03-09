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

import discord4j.common.json.response.WebhookResponse;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

public final class WebhookBean implements Serializable {

    private static final long serialVersionUID = 4352619598821771540L;

    private long id;
    private long guildId;
    private long channelId;
    private long user;
    @Nullable
    private String name;
    @Nullable
    private String avatar;
    private String token;

    public WebhookBean(final WebhookResponse response) {
        id = response.getId();
        guildId = response.getGuildId();
        channelId = response.getChannelId();
        user = Objects.requireNonNull(response.getUser()).getId();
        name = response.getName();
        avatar = response.getAvatar();
        token = response.getToken();
    }

    public WebhookBean() {}

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(final long guildId) {
        this.guildId = guildId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(final long channelId) {
        this.channelId = channelId;
    }

    public long getUser() {
        return user;
    }

    public void setUser(final long user) {
        this.user = user;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable final String name) {
        this.name = name;
    }

    @Nullable
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(@Nullable final String avatar) {
        this.avatar = avatar;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }
}
