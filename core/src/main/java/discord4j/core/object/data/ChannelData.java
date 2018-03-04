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
package discord4j.core.object.data;

import discord4j.common.json.OverwriteEntity;

import javax.annotation.Nullable;

public class ChannelData {

	private final long id;
	private final int type;
	@Nullable
	private final Long guildId;
	@Nullable
	private final Integer position;
	@Nullable
	private final OverwriteData[] permissionOverwrites;
	private final String name;
	private final String topic;
	@Nullable
	private final Boolean nsfw;
	@Nullable
	private final Long lastMessageId;
	@Nullable
	private final Integer bitrate;
	@Nullable
	private final Integer userLimit;
	@Nullable
	private final long[] recipients;
	@Nullable
	private final String icon;
	@Nullable
	private final Long ownerId;
	@Nullable
	private final Long applicationId;
	@Nullable
	private final Long parentId;
	@Nullable
	private final String lastPinTimestamp;

	public ChannelData(long id, int type, @Nullable Long guildId, @Nullable Integer position,
			@Nullable OverwriteData[] permissionOverwrites, String name, String topic, @Nullable Boolean nsfw,
			@Nullable Long lastMessageId, @Nullable Integer bitrate, @Nullable Integer userLimit,
			@Nullable long[] recipients, @Nullable String icon, @Nullable Long ownerId, @Nullable Long applicationId,
			@Nullable Long parentId, @Nullable String lastPinTimestamp) {
		this.id = id;
		this.type = type;
		this.guildId = guildId;
		this.position = position;
		this.permissionOverwrites = permissionOverwrites;
		this.name = name;
		this.topic = topic;
		this.nsfw = nsfw;
		this.lastMessageId = lastMessageId;
		this.bitrate = bitrate;
		this.userLimit = userLimit;
		this.recipients = recipients;
		this.icon = icon;
		this.ownerId = ownerId;
		this.applicationId = applicationId;
		this.parentId = parentId;
		this.lastPinTimestamp = lastPinTimestamp;
	}

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
	public OverwriteData[] getPermissionOverwrites() {
		return permissionOverwrites;
	}

	public String getName() {
		return name;
	}

	public String getTopic() {
		return topic;
	}

	@Nullable
	public Boolean getNsfw() {
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
	public long[] getRecipients() {
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
}
