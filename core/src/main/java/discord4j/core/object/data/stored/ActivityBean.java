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

import discord4j.common.json.EmojiResponse;
import discord4j.gateway.json.response.GameResponse;
import reactor.util.annotation.Nullable;

import java.io.Serializable;

public class ActivityBean implements Serializable {

    private static final long serialVersionUID = 9097728707632724692L;

    private String name;
    private int type;
    @Nullable
    private String url;
    @Nullable
    private String action;
    @Nullable
    private EmojiResponse emoji;

    public ActivityBean(final GameResponse response) {
        name = response.getName();
        type = response.getType();
        url = response.getUrl();
        action = response.getState();
        emoji = response.getEmoji();
    }

    public ActivityBean() {}

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(final int type) {
        this.type = type;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public void setUrl(@Nullable final String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "ActivityBean{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", url='" + url + '\'' +
                ", action='" + action + '\'' +
                ", emoji='" + emoji + '\'' +
                '}';
    }
}
