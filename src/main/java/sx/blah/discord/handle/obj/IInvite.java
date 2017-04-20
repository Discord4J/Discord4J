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
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

/**
 * Represents an invite into a channel.
 */
public interface IInvite {

	/**
	 * @return The invite code
	 */
	String getInviteCode();

	/**
	 * Gains the same information as accepting,
	 * but doesn't actually accept the invite.
	 *
	 * @return an InviteResponse containing the invite's details.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	InviteResponse details();

	/**
	 * Attempts to delete the invite this object represents.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void delete();

	/**
	 * This gets the client that this object is tied to.
	 *
	 * @return The client.
	 */
	IDiscordClient getClient();

	/**
	 * Represents the details of an invite.
	 */
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

		//TODO replace with objects. Need to figure out logistics, as the GUILD_CREATE is sent after MESSAGE_CREATE and after we accept the invite
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
