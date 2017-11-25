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
import discord4j.common.jackson.PossibleLong;
import discord4j.common.jackson.UnsignedJson;

import javax.annotation.Nullable;
import java.util.OptionalLong;

@PossibleJson
public class GuildMemberModifyRequest {

	@Nullable
	private final Possible<String> nick;
	@UnsignedJson
	private final Possible<long[]> roles;
	private final Possible<Boolean> mute;
	private final Possible<Boolean> deaf;
	@JsonProperty("channel_id")
	@Nullable
	@UnsignedJson
	private final PossibleLong channelId;

	public GuildMemberModifyRequest(@Nullable Possible<String> nick, Possible<long[]> roles,
	                                Possible<Boolean> mute, Possible<Boolean> deaf,
	                                @Nullable PossibleLong channelId) {
		this.nick = nick;
		this.roles = roles;
		this.mute = mute;
		this.deaf = deaf;
		this.channelId = channelId;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Possible<String> nick = Possible.absent();
		private Possible<long[]> roles = Possible.absent();
		private Possible<Boolean> mute = Possible.absent();
		private Possible<Boolean> deaf = Possible.absent();
		private PossibleLong channelId = PossibleLong.absent();

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

		public Builder channelId(OptionalLong channelId) {
			this.channelId = channelId.isPresent() ? PossibleLong.of(channelId.getAsLong()) : null;
			return this;
		}

		public GuildMemberModifyRequest build() {
			return new GuildMemberModifyRequest(nick, roles, mute, deaf, channelId);
		}
	}

	@Override
	public String toString() {
		return "GuildMemberModifyRequest[" +
				"nick=" + nick +
				", roles=" + roles +
				", mute=" + mute +
				", deaf=" + deaf +
				", channelId=" + channelId +
				']';
	}
}
