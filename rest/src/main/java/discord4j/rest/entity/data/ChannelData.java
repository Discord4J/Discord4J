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

import discord4j.common.json.UserResponse;
import discord4j.rest.json.response.ChannelResponse;

import java.util.Arrays;

public class ChannelData {

    private final long id;
    private final int type;
    private final boolean nsfw;
    private final Long guildId;
    private final Integer position;
    private final PermissionOverwriteData[] permissionOverwrites;
    private final String name;
    private final String topic;
    private final Long lastMessageId;
    private final Integer bitrate;
    private final Integer userLimit;
    private final Integer rateLimitPerUser;
    private final long[] recipients;
    private final Long parentId;
    private final String lastPinTimestamp;

    public ChannelData(ChannelResponse response) {
        id = response.getId();
        type = response.getType();
        guildId = response.getGuildId();
        position = response.getPosition();
        permissionOverwrites = response.getPermissionOverwrites() == null ? null :
                Arrays.stream(response.getPermissionOverwrites())
                        .map(PermissionOverwriteData::new)
                        .toArray(PermissionOverwriteData[]::new);
        name = response.getName();
        topic = response.getTopic();
        nsfw = response.isNsfw() != null && response.isNsfw();
        lastMessageId = response.getLastMessageId();
        bitrate = response.getBitrate();
        userLimit = response.getUserLimit();
        rateLimitPerUser = response.getRateLimitPerUser();
        recipients = response.getRecipients() == null ? null :
                Arrays.stream(response.getRecipients())
                        .mapToLong(UserResponse::getId)
                        .toArray();
        parentId = response.getParentId();
        lastPinTimestamp = response.getLastPinTimestamp();
    }

    public long getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public Long getGuildId() {
        return guildId;
    }

    public Integer getPosition() {
        return position;
    }

    public PermissionOverwriteData[] getPermissionOverwrites() {
        return permissionOverwrites;
    }

    public String getName() {
        return name;
    }

    public String getTopic() {
        return topic;
    }

    public Long getLastMessageId() {
        return lastMessageId;
    }

    public Integer getBitrate() {
        return bitrate;
    }

    public Integer getUserLimit() {
        return userLimit;
    }

    public Integer getRateLimitPerUser() {
        return rateLimitPerUser;
    }

    public long[] getRecipients() {
        return recipients;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getLastPinTimestamp() {
        return lastPinTimestamp;
    }

    @Override
    public String toString() {
        return "ChannelData{" +
                "id=" + id +
                ", type=" + type +
                ", nsfw=" + nsfw +
                ", guildId=" + guildId +
                ", position=" + position +
                ", permissionOverwrites=" + Arrays.toString(permissionOverwrites) +
                ", name='" + name + '\'' +
                ", topic='" + topic + '\'' +
                ", lastMessageId=" + lastMessageId +
                ", bitrate=" + bitrate +
                ", userLimit=" + userLimit +
                ", rateLimitPerUser=" + rateLimitPerUser +
                ", recipients=" + Arrays.toString(recipients) +
                ", parentId=" + parentId +
                ", lastPinTimestamp='" + lastPinTimestamp + '\'' +
                '}';
    }
}
