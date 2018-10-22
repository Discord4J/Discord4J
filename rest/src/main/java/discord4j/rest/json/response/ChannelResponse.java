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
package discord4j.rest.json.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.OverwriteEntity;
import discord4j.common.json.UserResponse;

import javax.annotation.Nullable;
import java.util.Arrays;

public class ChannelResponse {

    @UnsignedJson
    private long id;
    private int type;
    @JsonProperty("guild_id")
    @Nullable
    @UnsignedJson
    private Long guildId;
    @Nullable
    private Integer position;
    @JsonProperty("permission_overwrites")
    @Nullable
    private OverwriteEntity[] permissionOverwrites;
    @Nullable
    private String name;
    @Nullable
    private String topic;
    @Nullable
    private Boolean nsfw;
    @JsonProperty("last_message_id")
    @Nullable
    @UnsignedJson
    private Long lastMessageId;
    @Nullable
    private Integer bitrate;
    @JsonProperty("user_limit")
    @Nullable
    private Integer userLimit;
    @JsonProperty("rate_limit_per_user")
    @Nullable
    private Integer rateLimitPerUser;
    @Nullable
    private UserResponse[] recipients;
    @Nullable
    private String icon;
    @JsonProperty("owner_id")
    @Nullable
    @UnsignedJson
    private Long ownerId;
    @JsonProperty("application_id")
    @Nullable
    @UnsignedJson
    private Long applicationId;
    @JsonProperty("parent_id")
    @Nullable
    @UnsignedJson
    private Long parentId;
    @JsonProperty("last_pin_timestamp")
    @Nullable
    private String lastPinTimestamp;

    public long getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    @Nullable
    public Long getGuildId() {
        return guildId;
    }

    @Nullable
    public Integer getPosition() {
        return position;
    }

    @Nullable
    public OverwriteEntity[] getPermissionOverwrites() {
        return permissionOverwrites;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getTopic() {
        return topic;
    }

    @Nullable
    public Boolean isNsfw() {
        return nsfw;
    }

    @Nullable
    public Long getLastMessageId() {
        return lastMessageId;
    }

    @Nullable
    public Integer getBitrate() {
        return bitrate;
    }

    @Nullable
    public Integer getUserLimit() {
        return userLimit;
    }

    @Nullable
    public Integer getRateLimitPerUser() {
        return rateLimitPerUser;
    }

    @Nullable
    public UserResponse[] getRecipients() {
        return recipients;
    }

    @Nullable
    public String getIcon() {
        return icon;
    }

    @Nullable
    public Long getOwnerId() {
        return ownerId;
    }

    @Nullable
    public Long getApplicationId() {
        return applicationId;
    }

    @Nullable
    public Long getParentId() {
        return parentId;
    }

    @Nullable
    public String getLastPinTimestamp() {
        return lastPinTimestamp;
    }

    @Override
    public String toString() {
        return "ChannelResponse{" +
                "id=" + id +
                ", type=" + type +
                ", guildId=" + guildId +
                ", position=" + position +
                ", permissionOverwrites=" + Arrays.toString(permissionOverwrites) +
                ", name='" + name + '\'' +
                ", topic='" + topic + '\'' +
                ", nsfw=" + nsfw +
                ", lastMessageId=" + lastMessageId +
                ", bitrate=" + bitrate +
                ", userLimit=" + userLimit +
                ", rateLimitPerUser=" + rateLimitPerUser +
                ", recipients=" + Arrays.toString(recipients) +
                ", icon='" + icon + '\'' +
                ", ownerId=" + ownerId +
                ", applicationId=" + applicationId +
                ", parentId=" + parentId +
                ", lastPinTimestamp='" + lastPinTimestamp + '\'' +
                '}';
    }
}
