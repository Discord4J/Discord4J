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
import discord4j.common.json.OverwriteEntity;

import javax.annotation.Nullable;

public class ChannelResponse {

	private String id;
	private int type;
	@JsonProperty("guild_id")
	@Nullable
	private String guildId;
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
	@Nullable
	private String lastMessageId;
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
	@Nullable
	private String ownerId;
	@JsonProperty("application_id")
	@Nullable
	private String applicationId;
	@JsonProperty("parent_id")
	@Nullable
	private String parentId;

	public String getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	@Nullable
	public String getGuildId() {
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

	@Nullable
	public String getLastMessageId() {
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

	@Nullable
	public String getOwnerId() {
		return ownerId;
	}

	@Nullable
	public String getApplicationId() {
		return applicationId;
	}

	@Nullable
	public String getParentId() {
		return parentId;
	}
}
