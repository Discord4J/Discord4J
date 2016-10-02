package sx.blah.discord.api.internal.json.responses;

import sx.blah.discord.api.internal.json.generic.RoleResponse;

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
	 * The number of members in the guild.
	 */
	public int member_count;

	/**
	 * The members in the channel
	 */
	public MemberResponse[] members;

	/**
	 * Whether the the guild is large and requires a separate request to retrieve offline guild members.
	 */
	public boolean large = false;

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
	 * Custom emojis for the guild.
	 */
	public GuildEmojiUpdateResponse.EmojiObj[] emojis;

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

		/**
		 * The nickname for this user, it is null if non-existent (no such key on the actual json in that case).
		 */
		public String nick;

		public MemberResponse(UserResponse user, String[] roles) {
			this.user = user;
			this.roles = roles;
		}
	}
}
