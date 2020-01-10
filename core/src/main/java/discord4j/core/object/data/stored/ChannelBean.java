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
import discord4j.gateway.json.response.GatewayChannelResponse;
import discord4j.rest.json.response.ChannelResponse;
import reactor.util.annotation.Nullable;

import java.io.Serializable;
import java.util.Arrays;

public class ChannelBean implements Serializable {

    private static final long serialVersionUID = -8554782600684829226L;

    private long id;
    private int type;
    private boolean nsfw;

    @Nullable
    private Long guildId;
    @Nullable
    private Integer position;
    @Nullable
    private PermissionOverwriteBean[] permissionOverwrites;
    @Nullable
    private String name;
    @Nullable
    private String topic;
    @Nullable
    private Long lastMessageId;
    @Nullable
    private Integer bitrate;
    @Nullable
    private Integer userLimit;
    @Nullable
    private Integer rateLimitPerUser;
    @Nullable
    private long[] recipients;
    @Nullable
    private Long parentId;
    @Nullable
    private String lastPinTimestamp;

    public ChannelBean(GatewayChannelResponse channel, long guildId) {
        this(channel);
        this.guildId = guildId;
    }

    public ChannelBean(GatewayChannelResponse channel) {
        this(channel.getId(), channel.getType());

        position = channel.getPosition();
        permissionOverwrites = channel.getPermissionOverwrites() == null ? null :
                Arrays.stream(channel.getPermissionOverwrites())
                        .map(PermissionOverwriteBean::new)
                        .toArray(PermissionOverwriteBean[]::new);
        name = channel.getName();
        topic = channel.getTopic();
        nsfw = channel.getNsfw() != null && channel.getNsfw();
        lastMessageId = channel.getLastMessageId();
        bitrate = channel.getBitrate();
        userLimit = channel.getUserLimit();
        rateLimitPerUser = channel.getRateLimitPerUser();
        recipients = channel.getRecipients() == null ? null :
                Arrays.stream(channel.getRecipients())
                        .mapToLong(UserResponse::getId)
                        .toArray();
        parentId = channel.getParentId();
        lastPinTimestamp = channel.getLastPinTimestamp();
    }

    public ChannelBean(final ChannelResponse response) {
        this(response.getId(), response.getType());

        guildId = response.getGuildId();
        position = response.getPosition();
        permissionOverwrites = response.getPermissionOverwrites() == null ? null :
                Arrays.stream(response.getPermissionOverwrites())
                        .map(PermissionOverwriteBean::new)
                        .toArray(PermissionOverwriteBean[]::new);
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

    public ChannelBean(long id, int type) {
        this.id = id;
        this.type = type;
    }

    public ChannelBean() {}

    public final long getId() {
        return id;
    }

    public final void setId(final long id) {
        this.id = id;
    }

    public final int getType() {
        return type;
    }

    public final void setType(final int type) {
        this.type = type;
    }

    @Nullable
    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(@Nullable final Long guildId) {
        this.guildId = guildId;
    }

    @Nullable
    public Integer getPosition() {
        return position;
    }

    public void setPosition(@Nullable final Integer position) {
        this.position = position;
    }

    @Nullable
    public PermissionOverwriteBean[] getPermissionOverwrites() {
        return permissionOverwrites;
    }

    public void setPermissionOverwrites(@Nullable final PermissionOverwriteBean[] permissionOverwrites) {
        this.permissionOverwrites = permissionOverwrites;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable final String name) {
        this.name = name;
    }

    @Nullable
    public String getTopic() {
        return topic;
    }

    public void setTopic(@Nullable final String topic) {
        this.topic = topic;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public void setNsfw(final boolean nsfw) {
        this.nsfw = nsfw;
    }

    @Nullable
    public Long getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(@Nullable final Long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    @Nullable
    public Integer getBitrate() {
        return bitrate;
    }

    public void setBitrate(@Nullable final Integer bitrate) {
        this.bitrate = bitrate;
    }

    @Nullable
    public Integer getUserLimit() {
        return userLimit;
    }

    public void setUserLimit(@Nullable final Integer userLimit) {
        this.userLimit = userLimit;
    }

    @Nullable
    public Integer getRateLimitPerUser() {
        return rateLimitPerUser;
    }

    public void setRateLimitPerUser(@Nullable final Integer rateLimitPerUser) {
        this.rateLimitPerUser = rateLimitPerUser;
    }

    @Nullable
    public long[] getRecipients() {
        return recipients;
    }

    public void setRecipients(@Nullable final long[] recipients) {
        this.recipients = recipients;
    }

    @Nullable
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(@Nullable final Long parentId) {
        this.parentId = parentId;
    }

    @Nullable
    public String getLastPinTimestamp() {
        return lastPinTimestamp;
    }

    public void setLastPinTimestamp(@Nullable final String lastPinTimestamp) {
        this.lastPinTimestamp = lastPinTimestamp;
    }

    @Override
    public String toString() {
        return "ChannelBean{" +
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
