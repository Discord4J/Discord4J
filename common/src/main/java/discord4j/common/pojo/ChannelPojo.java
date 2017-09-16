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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.DiscordPojoFilter;
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleOptional;

/**
 * Represents a Channel Object as defined by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#channel-object">Channel Object</a>
 */
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DiscordPojoFilter.class)
public class ChannelPojo {

	// common
	private String id;
	private int type;
	private Possible<String> name;

	// guild only
	@JsonProperty("guild_id")
	private Possible<String> guildId;
	private Possible<Integer> position;

	// guild text only
	@JsonProperty("permission_overwrites")
	private Possible<OverwritePojo[]> permissionOverwrites;
	private Possible<String> topic;
	@JsonProperty("last_message_id")
	private Possible<String> lastMessageId;

	// guild voice only
	private Possible<Integer> bitrate;
	@JsonProperty("user_limit")
	private Possible<Integer> userLimit;

	// private/group only
	private Possible<UserPojo[]> recipients;
	private PossibleOptional<String> icon;
	@JsonProperty("owner_id")
	private Possible<String> ownerId;
	@JsonProperty("application_id")
	private Possible<String> applicationId;

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

	public Possible<String> getName() {
		return name;
	}

	public void setName(Possible<String> name) {
		this.name = name;
	}

	public Possible<String> getGuildId() {
		return guildId;
	}

	public void setGuildId(Possible<String> guildId) {
		this.guildId = guildId;
	}

	public Possible<Integer> getPosition() {
		return position;
	}

	public void setPosition(Possible<Integer> position) {
		this.position = position;
	}

	public Possible<OverwritePojo[]> getPermissionOverwrites() {
		return permissionOverwrites;
	}

	public void setPermissionOverwrites(Possible<OverwritePojo[]> permissionOverwrites) {
		this.permissionOverwrites = permissionOverwrites;
	}

	public Possible<String> getTopic() {
		return topic;
	}

	public void setTopic(Possible<String> topic) {
		this.topic = topic;
	}

	public Possible<String> getLastMessageId() {
		return lastMessageId;
	}

	public void setLastMessageId(Possible<String> lastMessageId) {
		this.lastMessageId = lastMessageId;
	}

	public Possible<Integer> getBitrate() {
		return bitrate;
	}

	public void setBitrate(Possible<Integer> bitrate) {
		this.bitrate = bitrate;
	}

	public Possible<Integer> getUserLimit() {
		return userLimit;
	}

	public void setUserLimit(Possible<Integer> userLimit) {
		this.userLimit = userLimit;
	}

	public Possible<UserPojo[]> getRecipients() {
		return recipients;
	}

	public void setRecipients(Possible<UserPojo[]> recipients) {
		this.recipients = recipients;
	}

	public PossibleOptional<String> getIcon() {
		return icon;
	}

	public void setIcon(PossibleOptional<String> icon) {
		this.icon = icon;
	}

	public Possible<String> getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Possible<String> ownerId) {
		this.ownerId = ownerId;
	}

	public Possible<String> getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Possible<String> applicationId) {
		this.applicationId = applicationId;
	}
}
