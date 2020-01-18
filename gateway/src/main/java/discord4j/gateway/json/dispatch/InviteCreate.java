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
import discord4j.common.json.UserResponse;

public class InviteCreate implements Dispatch {

    @JsonProperty("guild_id")
    @UnsignedJson
    private long guildId;
    @JsonProperty("channel_id")
    @UnsignedJson
    private long channelId;
    private String code;
    @JsonProperty("created_at")
    private String createdAt;
    private int uses;
    @JsonProperty("max_uses")
    private int maxUses;
    @JsonProperty("max_age")
    private int maxAge;
    private boolean temporary;
    private UserResponse inviter;

    public UserResponse getInviter() {
        return inviter;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getChannelId() {
        return channelId;
    }

    public String getCode() {
        return code;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getUses() {
        return uses;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public boolean isTemporary() {
        return temporary;
    }

    @Override
    public String toString() {
        return "InviteCreate{" +
            "code='" + code + '\'' +
            ", guildId=" + guildId +
            ", channelId=" + channelId +
            ", inviter=" + inviter +
            ", uses=" + uses +
            ", maxUses=" + maxUses +
            ", maxAge=" + maxAge +
            ", temporary=" + temporary +
            ", createdAt='" + createdAt +
            '}';
    }
}
