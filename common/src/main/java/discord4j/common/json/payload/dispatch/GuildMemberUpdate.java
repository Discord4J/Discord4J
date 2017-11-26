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
package discord4j.common.json.payload.dispatch;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.response.RoleResponse;
import discord4j.common.json.response.UserResponse;

import java.util.Arrays;

public class GuildMemberUpdate implements Dispatch {

	@JsonProperty("guild_id")
	@UnsignedJson
	private long guildId;
	private RoleResponse[] roles;
	private UserResponse[] user;
	private String nick;

	public long getGuildId() {
		return guildId;
	}

	public RoleResponse[] getRoles() {
		return roles;
	}

	public UserResponse[] getUser() {
		return user;
	}

	public String getNick() {
		return nick;
	}

	@Override
	public String toString() {
		return "GuildMemberUpdate[" +
				"guildId=" + guildId +
				", roles=" + Arrays.toString(roles) +
				", user=" + Arrays.toString(user) +
				", nick=" + nick +
				']';
	}
}
