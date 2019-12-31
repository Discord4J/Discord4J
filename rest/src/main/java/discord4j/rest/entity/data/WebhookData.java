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

import discord4j.rest.json.response.WebhookResponse;
import reactor.util.annotation.Nullable;

import java.util.Objects;

public class WebhookData {

    private final long id;
    private final long guildId;
    private final long channelId;
    private final long user;
    @Nullable
    private final String name;
    @Nullable
    private final String avatar;
    private final String token;

    public WebhookData(WebhookResponse response) {
        id = response.getId();
        guildId = response.getGuildId();
        channelId = response.getChannelId();
        user = Objects.requireNonNull(response.getUser()).getId();
        name = response.getName();
        avatar = response.getAvatar();
        token = response.getToken();
    }

    public long getId() {
        return id;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getChannelId() {
        return channelId;
    }

    public long getUser() {
        return user;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getAvatar() {
        return avatar;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "WebhookData{" +
                "id=" + id +
                ", guildId=" + guildId +
                ", channelId=" + channelId +
                ", user=" + user +
                ", name='" + name + '\'' +
                ", avatar='" + avatar + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
