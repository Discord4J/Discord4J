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
package discord4j.gateway.json.dispatch;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;

public class InviteDelete implements Dispatch {

    @JsonProperty("guild_id")
    @UnsignedJson
    private long guildId;
    @JsonProperty("channel_id")
    @UnsignedJson
    private long channelId;
    private String code;

    public long getGuildId() {
        return guildId;
    }

    public long getChannelId() {
        return channelId;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "InviteDelete{" +
            "code='" + code + '\'' +
            ", guildId=" + guildId +
            ", channelId=" + channelId +
            '}';
    }
}
