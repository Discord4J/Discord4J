package sx.blah.discord.json.responses;

/**
 * Response representing a guild
 */
public class GuildResponse {
	
	/**
	 * True if the guild is unavailable due to an outage or something similar.
	 */
	public boolean unavailable = false;
	
	/**
	 * The voice states in the guild
	 */
	public VoiceStateResponse[] voice_states;
	
	/**
	 * Only not null for Discord Partner servers TODO: Verify that this is in fact a string
	 */
	public String splash;
	
	/**
	 * The roles in the channel
	 */
	public RoleResponse[] roles;
	
	/**
	 * The region the guild is hosted on
	 */
	public String region;
	
	/**
	 * The presences of the users on the channel
	 */
	public PresenceResponse[] presences;
	
	/**
	 * The guild owner's id
	 */
	public String owner_id;
	
	/**
	 * The guild's name
	 */
	public String name;
	
	/**
	 * The members in the channel
	 */
	public MemberResponse[] members;
	
	/**
	 * FIXME ??
	 */
	public boolean large;
	
	/**
	 * The timestamp for when the guild was created
	 */
	public String joined_at;
	
	/**
	 * The guild id
	 */
	public String id;
	
	/**
	 * The guild's icon id
	 */
	public String icon;
	
	/**
	 * FIXME ??
	 */
//	public thing[] features;
	
	/**
	 * FIXME ??
	 */
//	public thing[] emojis;
	
	/**
	 * The channels in the guild
	 */
	public ChannelResponse[] channels;
	
	/**
	 * The timeout before a user is placed in the AFK channel (in seconds)
	 */
	public int afk_timeout;
	
	/**
	 * The afk channel's id
	 */
	public String afk_channel_id;
	
	/**
	 * Represents a voice state
	 */
	public class VoiceStateResponse {
		
		/**
		 * The user's id
		 */
		public String user_id;
		
		/**
		 * From @Voltana: afk channel, it's a temporary mute/deaf
		 */
		public boolean suppress;
		
		/**
		 * Voice session id
		 */
		public String session_id;
		
		/**
		 * Whether the user muted him/herself
		 */
		public boolean self_mute;
		
		/**
		 * Whether the user deafened him/herself
		 */
		public boolean self_deaf;
		
		/**
		 * Whether the user is muted by the server
		 */
		public boolean mute;
		
		/**
		 * Whether the user is deafened by the server
		 */
		public boolean deaf;
		
		/**
		 * The voice channel's id
		 */
		public String channel_id;
	}
	
	/**
	 * Represents a role
	 */
	public class RoleResponse {
		
		/**
		 * Where the role should be displayed. -1 is @everyone, it is always last
		 */
		public int position;
		
		/**
		 * The permissions the user has. See http://bit.ly/1OnxVZe. To see if the user has a permission,
		 * {@code 1 << [permOffset] & [permissions number] must be greater than 0. Remember to check the manage roles permission!}
		 * That permissions supercedes all others
		 */
		public int permissions;
		
		/**
		 * The role name
		 */
		public String name;
		
		/**
		 * Whether this role is managed via plugins like twitch
		 */
		public boolean managed;
		
		/**
		 * The role id
		 */
		public String id;
		
		/**
		 * Whether to display this role separately from others
		 */
		public boolean hoist;
		
		/**
		 * The DECIMAL format for the color
		 */
		public int color;
	}
	
	/**
	 * Represents a guild member
	 */
	public static class MemberResponse {
		
		/**
		 * The user this member object represents
		 */
		public UserResponse user;
		
		/**
		 * The roles this member is a part of
		 */
		public String[] roles;
		
		/**
		 * Whether the user is muted
		 */
		public boolean mute;
		
		/**
		 * Whether the user is deafened
		 */
		public boolean deaf;
		
		/**
		 * The timestamp for when the user joined the guild
		 */
		public String joined_at;
		
		public MemberResponse(UserResponse user, String[] roles) {
			this.user = user;
			this.roles = roles;
		}
	}
}
