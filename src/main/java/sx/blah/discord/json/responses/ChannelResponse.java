package sx.blah.discord.json.responses;

/**
 * Represents a channel
 */
public class ChannelResponse {
	
	/**
	 * The channel type, either "text" or "voice"
	 */
	public String type;
	
	/**
	 * The guild this channel belongs to.
	 */
	public String guild_id;
	
	/**
	 * The channel topic, can be null
	 */
	public String topic;
	
	/**
	 * The relative position of the channel on the channels list
	 */
	public int position;
	
	/**
	 * Overwritten permissions in the channel
	 */
	public PermissionOverwriteResponse[] permission_overwrites;
	
	/**
	 * The channel name
	 */
	public String name;
	
	/**
	 * The last message's id
	 */
	public String last_message_id;
	
	/**
	 * The channel's id
	 */
	public String id;
	
	public class PermissionOverwriteResponse {
		
		/**
		 * The permission type, either "role" or "member"
		 */
		public String type;
		
		/**
		 * Either the role or user id
		 */
		public String id;
		
		/**
		 * Permissions to deny, see {@link GuildResponse.RoleResponse#permissions}
		 */
		public int deny;
		
		/**
		 * Permissions to allow, see {@link GuildResponse.RoleResponse#permissions}
		 */
		public int allow;
	}
}
