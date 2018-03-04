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

import javax.annotation.Nullable;
import java.util.Arrays;

public class PresenceResponse {

	private UserResponse user;
	@UnsignedJson
	@Nullable
	private long[] roles;
	@Nullable
	private GameResponse game;
	@JsonProperty("guild_id")
	@Nullable
	@UnsignedJson
	private Long guildId;
	@Nullable
	private String status;

	public UserResponse getUser() {
		return user;
	}

	@Nullable
	public long[] getRoles() {
		return roles;
	}

	@Nullable
	public GameResponse getGame() {
		return game;
	}

	@Nullable
	public Long getGuildId() {
		return guildId;
	}

	@Nullable
	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "PresenceResponse[" +
				"user=" + user +
				", roles=" + Arrays.toString(roles) +
				", game=" + game +
				", guildId=" + guildId +
				", status=" + status +
				']';
	}
}
