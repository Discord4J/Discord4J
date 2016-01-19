package sx.blah.discord.handle.obj;

import sx.blah.discord.util.HTTP403Exception;

/**
 * Represents an invite into a channel.
 */
public interface IInvite {
	
	/**
	 * @return The invite code
	 */
	String getInviteCode();
	
	/**
	 * @return The xkcd pass, this is null if it doesn't exist!
	 */
	String getXkcdPass();
	
	/**
	 * @return Accepts the invite and returns relevant information,
	 * such as the Guild ID and name, and the channel the invite
	 * was created from.
	 *
	 * @throws Exception
	 */
	InviteResponse accept() throws Exception;
	
	/**
	 * Gains the same information as accepting,
	 * but doesn't actually accept the invite.
	 *
	 * @return an InviteResponse containing the invite's details.
	 *
	 * @throws Exception
	 */
	InviteResponse details() throws Exception;
	
	/**
	 * Attempts to delete the invite this object represents.
	 *
	 * @throws HTTP403Exception
	 */
	void delete() throws HTTP403Exception;
	
	/**
	 * Represents the details of an invite.
	 */
	class InviteResponse {
		
		/**
		 * ID of the guild you were invited to.
		 */
		private final String guildID;
		
		/**
		 * Name of the guild you were invited to.
		 */
		private final String guildName;
		
		/**
		 * ID of the channel you were invited from.
		 */
		private final String channelID;
		
		/**
		 * Name of the channel you were invited from.
		 */
		private final String channelName;
		
		//TODO replace with objects. Need to figure out logistics, as the GUILD_CREATE is sent after MESSAGE_CREATE and after we accept the invite
		public InviteResponse(String guildID, String guildName, String channelID, String channelName) {
			this.guildID = guildID;
			this.guildName = guildName;
			this.channelID = channelID;
			this.channelName = channelName;
		}
		
		/**
		 * Gets the guild id the invite leads to.
		 *
		 * @return The guild id.
		 */
		public String getGuildID() {
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
		 */
		public String getChannelID() {
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
