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

package sx.blah.discord.handle.obj;

import sx.blah.discord.api.IDiscordClient;

/**
 * An invite to a guild channel.
 */
public interface IInvite {

	/**
	 * Gets the invite code for the invite.
	 *
	 * @return The invite code for the invite.
	 */
	String getCode();

	/**
	 * Gets the guild the invite is for.
	 *
	 * @return The guild the invite is for.
	 */
	IGuild getGuild();

	/**
	 * Gets the channel the invite is for.
	 *
	 * @return The channel the invite is for.
	 */
	IChannel getChannel();

	/**
	 * Gets the user who created the invite.
	 *
	 * <p>This is null for vanity url invites and widget invites.
	 *
	 * @return The user who created the invite.
	 */
	IUser getInviter();

	/**
	 * Gets the client the invite belongs to.
	 *
	 * @return The client the invite belongs to.
	 */
	IDiscordClient getClient();

	/**
	 * Deletes the invite.
	 */
	void delete();

	/**
	 * Gets the invite code for the invite.
	 *
	 * @return The invite code for the invite.
	 * @deprecated Use {@link #getCode()} instead.
	 */
	@Deprecated
	String getInviteCode();

	/**
	 * Gets extra details about the invite.
	 *
	 * @return Extra details about the invite.
	 * @deprecated <b>This method always returns null.</b> This is no longer needed as the same information and more can
	 * be obtained with methods in {@link IInvite} and {@link IExtendedInvite}.
	 */
	@Deprecated
	InviteResponse details();

	@Deprecated
	class InviteResponse {

		/**
		 * ID of the guild you were invited to.
		 */
		private final long guildID;

		/**
		 * Name of the guild you were invited to.
		 */
		private final String guildName;

		/**
		 * ID of the channel you were invited from.
		 */
		private final long channelID;

		/**
		 * Name of the channel you were invited from.
		 */
		private final String channelName;

		public InviteResponse(long guildID, String guildName, long channelID, String channelName) {
			this.guildID = guildID;
			this.guildName = guildName;
			this.channelID = channelID;
			this.channelName = channelName;
		}

		/**
		 * Gets the guild id the invite leads to.
		 *
		 * @return The guild id.
		 * @deprecated Use {@link #getGuildLongID()} instead
		 */
		@Deprecated
		public String getGuildID() {
			return Long.toUnsignedString(getGuildLongID());
		}

		/**
		 * Gets the guild id the invite leads to.
		 *
		 * @return The guild id.
		 */
		public long getGuildLongID() {
			return guildID;
		}

		/**
		 * Gets the name of the guild the invite leads to.
		 *
		 * @return The guild name.
		 */
		public String getGuildName() {
			return guildName;
		}

		/**
		 * Gets the channel id the invite leads to.
		 *
		 * @return The channel id.
		 * @deprecated Use {@link #getChannelLongID()} instead
		 */
		@Deprecated
		public String getChannelID() {
			return Long.toUnsignedString(getChannelLongID());
		}

		/**
		 * Gets the channel id the invite leads to.
		 *
		 * @return The channel id.
		 */
		public long getChannelLongID() {
			return channelID;
		}

		/**
		 * Gets the channel name the invite leads to.
		 *
		 * @return The channel name.
		 */
		public String getChannelName() {
			return channelName;
		}
	}
}
