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

	@JsonProperty("accessToken")
	private final String accessToken;
	@Nullable
	private final Possible<String> nick;
	@UnsignedJson
	private final Possible<long[]> roles;
	private final Possible<Boolean> mute;
	private final Possible<Boolean> deaf;

	public GuildMemberAddRequest(String accessToken, @Nullable Possible<String> nick,
	                             Possible<long[]> roles, Possible<Boolean> mute,
	                             Possible<Boolean> deaf) {
		this.accessToken = accessToken;
		this.nick = nick;
		this.roles = roles;
		this.mute = mute;
		this.deaf = deaf;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String accessToken;
		private Possible<String> nick = Possible.absent();
		private Possible<long[]> roles = Possible.absent();
		private Possible<Boolean> mute = Possible.absent();
		private Possible<Boolean> deaf = Possible.absent();

		public Builder accessToken(String accessToken) {
			this.accessToken = accessToken;
			return this;
		}

		public Builder nick(String nick) {
			this.nick = Possible.of(nick);
			return this;
		}

		public Builder roles(long[] roles) {
			this.roles = Possible.of(roles);
			return this;
		}

		public Builder mute(boolean mute) {
			this.mute = Possible.of(mute);
			return this;
		}

		public Builder deaf(boolean deaf) {
			this.deaf = Possible.of(deaf);
			return this;
		}

		public GuildMemberAddRequest build() {
			return new GuildMemberAddRequest(accessToken, nick, roles, mute, deaf);
		}
	}
}
