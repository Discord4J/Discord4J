package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json private channel object.
 * Used for convenience.
 */
public class PrivateChannelObject {
	/**
	 * The id of the last message sent in the channel.
	 */
	public String last_message_id;
	/**
	 * the recipient of the channel.
	 */
	public UserObject recipient;
	/**
	 * The id of the channel.
	 */
	public String id;
	/**
	 * Whether the channel is private.
	 */
	public boolean is_private;
}
