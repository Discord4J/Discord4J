/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.api.internal.json.requests;

import sx.blah.discord.handle.obj.IRole;

import java.util.Arrays;

/**
 * Sent to edit a guild member's properties.
 */
public class MemberEditRequest {

	public static class Builder {

		private IRole[] roles;
		private String nick;
		private Boolean mute;
		private Boolean deafen;
		private String channelID;

		/**
		 * Sets the roles for the user to have.
		 *
		 * @param roles An array of Role objects.
		 * @return This builder, for chaining.
		 */
		public Builder roles(IRole[] roles) {
			this.roles = roles;
			return this;
		}

		/**
		 * Sets the user's nickname.
		 *
		 * @param nick The new user nickname.
		 * @return This builder, for chaining.
		 */
		public Builder nick(String nick) {
			this.nick = nick;
			return this;
		}

		/**
		 * Sets whether to mute the user.
		 *
		 * @param mute If the user should be muted.
		 * @return This builder, for chaining.
		 */
		public Builder mute(boolean mute) {
			this.mute = mute;
			return this;
		}

		/**
		 * Sets whether to deafen the user.
		 *
		 * @param deafen If the user should be deafened.
		 * @return This builder, for chaining.
		 */
		public Builder deafen(boolean deafen) {
			this.deafen = deafen;
			return this;
		}

		/**
		 * Sets the voice channel to move the user to.
		 *
		 * @param channelID The target voice channel ID to move the user to.
		 * @return This builder, for chaining.
		 */
		public Builder channel(String channelID) {
			this.channelID = channelID;
			return this;
		}

		/**
		 * Builds the request object.
		 *
		 * @return The member edit request.
		 */
		public MemberEditRequest build() {
			return new MemberEditRequest(roles, nick, mute, deafen, channelID);
		}
	}

	/**
	 * The member's roles.
	 */
	private final String[] roles;
	/**
	 * The member's nickname.
	 */
	private final String nick;
	/**
	 * Whether the member is muted.
	 */
	private final Boolean mute;
	/**
	 * Whether the member is deafened.
	 */
	private final Boolean deaf;
	/**
	 * The ID the voice channel the member is in.
	 */
	private final String channel_id;

	MemberEditRequest(IRole[] roles, String nick, Boolean mute, Boolean deaf, String channelID) {
		this.roles = roles == null ? null : Arrays.stream(roles).map(IRole::getStringID).distinct().toArray(String[]::new);
		this.nick = nick;
		this.mute = mute;
		this.deaf = deaf;
		this.channel_id = channelID;
	}

	public String[] getRoles() {
		return roles;
	}

	public String getNick() {
		return nick;
	}

	public Boolean getMute() {
		return mute;
	}

	public Boolean getDeaf() {
		return deaf;
	}

	public String getChannelID() {
		return channel_id;
	}
}
