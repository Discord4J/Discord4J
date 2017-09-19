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
import discord4j.common.jackson.DiscordPojo;

import java.util.Optional;

/**
 * Represents a Presence Update Object as defined by Discord.
 *
 * @see
 * <a href="https://discordapp.com/developers/docs/topics/gateway#presence-update-presence-update-event-fields">Presence
 * Update Object</a>
 */
@DiscordPojo
public class PresencePojo {

	private UserPojo user;
	private String[] roles;
	private Optional<GamePojo> game;
	@JsonProperty("guild_id")
	private String guildId;
	private String status;

	public UserPojo getUser() {
		return user;
	}

	public void setUser(UserPojo user) {
		this.user = user;
	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	public Optional<GamePojo> getGame() {
		return game;
	}

	public void setGame(Optional<GamePojo> game) {
		this.game = game;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
