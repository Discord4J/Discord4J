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
package discord4j.common.json.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.OverwriteEntity;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.OptionalLong;

public class ChannelResponse {

	@UnsignedJson
	private long id;
	private int type;
	@JsonProperty("guild_id")
	@UnsignedJson
	private OptionalLong guildId;
	@Nullable
	private Integer position;
	@JsonProperty("permission_overwrites")
	@Nullable
	private OverwriteEntity[] permissionOverwrites;
	private String name;
	@Nullable
	private String topic;
	@Nullable
	private Boolean nsfw;
	@JsonProperty("last_message_id")
	@UnsignedJson
	private OptionalLong lastMessageId;
	@Nullable
	private Integer bitrate;
	@JsonProperty("user_limit")
	@Nullable
	private Integer userLimit;
	@Nullable
	private UserResponse[] recipients;
	@Nullable
	private String icon;
	@JsonProperty("owner_id")
	@UnsignedJson
	private OptionalLong ownerId;
	@JsonProperty("application_id")
	@UnsignedJson
	private OptionalLong applicationId;
	@JsonProperty("parent_id")
	@UnsignedJson
	private OptionalLong parentId;
	@JsonProperty("last_pin_timestamp")
	@Nullable
	private String lastPinTimestamp;

	public long getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public OptionalLong getGuildId() {
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

	public String getName() {
		return name;
	}

	@Nullable
	public String getTopic() {
		return topic;
	}

	@Nullable
	public Boolean getNsfw() {
		return nsfw;
	}

	public OptionalLong getLastMessageId() {
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
	public UserResponse[] getRecipients() {
		return recipients;
	}

	@Nullable
	public String getIcon() {
		return icon;
	}

	public OptionalLong getOwnerId() {
		return ownerId;
	}

	public OptionalLong getApplicationId() {
		return applicationId;
	}

	public OptionalLong getParentId() {
		return parentId;
	}

	@Nullable
	public String getLastPinTimestamp() {
		return lastPinTimestamp;
	}

	@Override
	public String toString() {
		return "ChannelResponse[" +
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
				", recipients=" + Arrays.toString(recipients) +
				", icon='" + icon + '\'' +
				", ownerId=" + ownerId +
				", applicationId=" + applicationId +
				", parentId=" + parentId +
				", lastPinTimestamp='" + lastPinTimestamp + '\'' +
				']';
	}
}
