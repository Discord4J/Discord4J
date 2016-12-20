package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json channel object.
 */
public class ChannelObject {
	/**
	 * The id of the channel.
	 */
	public String id;
	/**
	 * The id of the guild this channel is in.
	 */
	public String guild_id;
	/**
	 * The name of the channel.
	 */
	public String name;
	/**
	 * The type of the channel.
	 */
	public String type;
	/**
	 * The position of the channel.
	 */
	public int position;
	/**
	 * Whether the channel is private or not.
	 */
	public boolean is_private;
	/**
	 * Array of permission overwrites.
	 */
	public OverwriteObject[] permission_overwrites;
	/**
	 * Topic of the channel.
	 */
	public String topic;
	/**
	 * ID of the last message sent in the channel.
	 */
	public String last_message_id;
	/**
	 * When the last pin was made in the channel.
	 */
	public String last_pin_timestamp;
	/**
	 * Bitrate of the channel if it is voice type.
	 */
	public int bitrate;
	/**
	 * Maximum number of users allowed in the channel if it is voice type.
	 */
	public int user_limit;
	/**
	 * Recipients of the channel if it is private type.
	 */
	public UserObject[] recipients;
}
