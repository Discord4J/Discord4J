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
package discord4j.common.json.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleJson;
import discord4j.common.jackson.UnsignedJson;

import javax.annotation.Nullable;

@PossibleJson
public class GuildMemberAddRequest {

	@JsonProperty("access_token")
	private final String access_token;
	@Nullable
	private final Possible<String> nick;
	@UnsignedJson
	private final Possible<long[]> roles;
	private final Possible<Boolean> mute;
	private final Possible<Boolean> deaf;

	public GuildMemberAddRequest(String access_token, @Nullable Possible<String> nick,
	                             Possible<long[]> roles, Possible<Boolean> mute,
	                             Possible<Boolean> deaf) {
		this.access_token = access_token;
		this.nick = nick;
		this.roles = roles;
		this.mute = mute;
		this.deaf = deaf;
	}
}
