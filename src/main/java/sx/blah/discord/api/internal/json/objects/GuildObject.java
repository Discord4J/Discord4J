package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json guild object.
 */
public class GuildObject {
	/**
	 * The id of the guild.
	 */
	public String id;
	/**
	 * The name of the guild.
	 */
	public String name;
	/**
	 * The icon of the guild.
	 */
	public String icon;
	/**
	 * The id of the user that owns the guild.
	 */
	public String owner_id;
	/**
	 * The region the guild's voice server is in.
	 */
	public String region;
	/**
	 * The id of the afk voice channel.
	 */
	public String afk_channel_id;
	/**
	 * The timeout for moving people to the afk voice channel.
	 */
	public int afk_timeout;
	/**
	 * Whether this guild is embeddable via a widget.
	 */
	public boolean embed_enabled;
	/**
	 * The id of the embedded channel.
	 */
	public String embed_channel_id;
	/**
	 * Level of verification.
	 */
	public int verification_level;
	/**
	 * Default message notifications level.
	 */
	public int default_messages_notifications;
	/**
	 * Array of role objects
	 */
	public RoleObject[] roles;
	/**
	 * Array of emoji objects.
	 */
	public EmojiObject[] emojis;
	/**
	 * Array of guild features.
	 */
	public String[] features;
	/**
	 * Required MFA level for the guild.
	 */
	public int mfa_level;
	/**
	 * The date the self user joined the guild.
	 */
	public String joined_at;
	/**
	 * Whether the guild is considered to be large.
	 */
	public boolean large;
	/**
	 * Whether the guild is unavailable.
	 */
	public boolean unavailable;
	/**
	 * Number of members in the guild.
	 */
	public int member_count;
	/**
	 * Array of voice states for the members in the guild.
	 */
	public VoiceStateObject[] voice_states;
	/**
	 * Array of members.
	 */
	public MemberObject[] members;
	/**
	 * Array of channels.
	 */
	public ChannelObject[] channels;
	/**
	 * Array of presences for members in the guild.
	 */
	public PresenceObject[] presences;
}
