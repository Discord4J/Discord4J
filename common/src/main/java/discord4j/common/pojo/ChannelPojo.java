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
package discord4j.common.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.OptionalField;

public class ChannelPojo {

	// common
	private String id;
	private int type;
	private OptionalField<String> name;

	// guild only
	@JsonProperty("guild_id")
	private OptionalField<String> guildId;
	private OptionalField<Integer> position;

	// guild text only
	@JsonProperty("permission_overwrites")
	private OptionalField<OverwritePojo[]> permissionOverwrites;
	private OptionalField<String> topic;
	@JsonProperty("last_message_id")
	private OptionalField<String> lastMessageId;

	// guild voice only
	private OptionalField<Integer> bitrate;
	@JsonProperty("user_limit")
	private OptionalField<Integer> userLimit;

	// private/group only
	private OptionalField<UserPojo[]> recipients;
	private OptionalField<String> icon;
	@JsonProperty("owner_id")
	private OptionalField<String> ownerId;
	@JsonProperty("application_id")
	private OptionalField<String> applicationId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public OptionalField<String> getName() {
		return name;
	}

	public void setName(OptionalField<String> name) {
		this.name = name;
	}

	public OptionalField<String> getGuildId() {
		return guildId;
	}

	public void setGuildId(OptionalField<String> guildId) {
		this.guildId = guildId;
	}

	public OptionalField<Integer> getPosition() {
		return position;
	}

	public void setPosition(OptionalField<Integer> position) {
		this.position = position;
	}

	public OptionalField<OverwritePojo[]> getPermissionOverwrites() {
		return permissionOverwrites;
	}

	public void setPermissionOverwrites(OptionalField<OverwritePojo[]> permissionOverwrites) {
		this.permissionOverwrites = permissionOverwrites;
	}

	public OptionalField<String> getTopic() {
		return topic;
	}

	public void setTopic(OptionalField<String> topic) {
		this.topic = topic;
	}

	public OptionalField<String> getLastMessageId() {
		return lastMessageId;
	}

	public void setLastMessageId(OptionalField<String> lastMessageId) {
		this.lastMessageId = lastMessageId;
	}

	public OptionalField<Integer> getBitrate() {
		return bitrate;
	}

	public void setBitrate(OptionalField<Integer> bitrate) {
		this.bitrate = bitrate;
	}

	public OptionalField<Integer> getUserLimit() {
		return userLimit;
	}

	public void setUserLimit(OptionalField<Integer> userLimit) {
		this.userLimit = userLimit;
	}

	public OptionalField<UserPojo[]> getRecipients() {
		return recipients;
	}

	public void setRecipients(OptionalField<UserPojo[]> recipients) {
		this.recipients = recipients;
	}

	public OptionalField<String> getIcon() {
		return icon;
	}

	public void setIcon(OptionalField<String> icon) {
		this.icon = icon;
	}

	public OptionalField<String> getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(OptionalField<String> ownerId) {
		this.ownerId = ownerId;
	}

	public OptionalField<String> getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(OptionalField<String> applicationId) {
		this.applicationId = applicationId;
	}
}
