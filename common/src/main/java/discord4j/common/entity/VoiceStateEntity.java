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
package discord4j.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.DiscordEntity;
import discord4j.common.jackson.Possible;

/**
 * Represents a Voice State Entity as defined by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/voice#voice-state-object">Voice State Entity</a>
 */
@DiscordEntity
public class VoiceStateEntity {

	@JsonProperty("guild_id")
	private Possible<String> guildId = Possible.absent();
	@JsonProperty("channel_id")
	private String channelId;
	@JsonProperty("user_id")
	private String userId;
	@JsonProperty("session_id")
	private String sessionId;
	private boolean deaf;
	private boolean mute;
	@JsonProperty("self_deaf")
	private boolean selfDeaf;
	@JsonProperty("self_mute")
	private boolean selfMute;
	private boolean suppress;

	public Possible<String> getGuildId() {
		return guildId;
	}

	public void setGuildId(Possible<String> guildId) {
		this.guildId = guildId;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public boolean isDeaf() {
		return deaf;
	}

	public void setDeaf(boolean deaf) {
		this.deaf = deaf;
	}

	public boolean isMute() {
		return mute;
	}

	public void setMute(boolean mute) {
		this.mute = mute;
	}

	public boolean isSelfDeaf() {
		return selfDeaf;
	}

	public void setSelfDeaf(boolean selfDeaf) {
		this.selfDeaf = selfDeaf;
	}

	public boolean isSelfMute() {
		return selfMute;
	}

	public void setSelfMute(boolean selfMute) {
		this.selfMute = selfMute;
	}

	public boolean isSuppress() {
		return suppress;
	}

	public void setSuppress(boolean suppress) {
		this.suppress = suppress;
	}
}
