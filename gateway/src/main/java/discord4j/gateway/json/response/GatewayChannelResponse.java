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
package discord4j.gateway.json.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.OverwriteEntity;
import discord4j.common.json.UserResponse;

import javax.annotation.Nullable;
import java.util.Arrays;

public class GatewayChannelResponse {

    @JsonProperty("user_limit")
    @Nullable
    private Integer userLimit;
    private int type;
    private String topic;
    private int position;
    @JsonProperty("permission_overwrites")
    private OverwriteEntity[] permissionOverwrites;
    @JsonProperty("parent_id")
    @Nullable
    @UnsignedJson
    private Long parentId;
    @Nullable
    private Boolean nsfw;
    private String name;
    @JsonProperty("last_pin_timestamp")
    @Nullable
    private String lastPinTimestamp;
    @JsonProperty("last_message_id")
    @Nullable
    @UnsignedJson
    private Long lastMessageId;
    @UnsignedJson
    private long id;
    @Nullable
    private Integer bitrate;
    @Nullable
    private UserResponse[] recipients;
    @JsonProperty("rate_limit_per_user")
    @Nullable
    private Integer rateLimitPerUser;

    @Nullable
    public Integer getUserLimit() {
        return userLimit;
    }

    public int getType() {
        return type;
    }

    public String getTopic() {
        return topic;
    }

    public int getPosition() {
        return position;
    }

    public OverwriteEntity[] getPermissionOverwrites() {
        return permissionOverwrites;
    }

    @Nullable
    public Long getParentId() {
        return parentId;
    }

    @Nullable
    public Boolean getNsfw() {
        return nsfw;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getLastPinTimestamp() {
        return lastPinTimestamp;
    }

    @Nullable
    public Long getLastMessageId() {
        return lastMessageId;
    }

    public long getId() {
        return id;
    }

    @Nullable
    public Integer getBitrate() {
        return bitrate;
    }

    @Nullable
    public UserResponse[] getRecipients() {
        return recipients;
    }

    @Nullable
    public Integer getRateLimitPerUser() {
        return rateLimitPerUser;
    }

    @Override
    public String toString() {
        return "GatewayChannelResponse{" +
                "userLimit=" + userLimit +
                ", type=" + type +
                ", topic='" + topic + '\'' +
                ", position=" + position +
                ", permissionOverwrites=" + Arrays.toString(permissionOverwrites) +
                ", parentId=" + parentId +
                ", nsfw=" + nsfw +
                ", name='" + name + '\'' +
                ", lastPinTimestamp='" + lastPinTimestamp + '\'' +
                ", lastMessageId=" + lastMessageId +
                ", id=" + id +
                ", bitrate=" + bitrate +
                ", recipients=" + Arrays.toString(recipients) +
                ", rateLimitPerUser=" + rateLimitPerUser +
                '}';
    }
}
