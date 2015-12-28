package sx.blah.discord.json.responses;

/**
 * The response received when receiving private channel info
 */
public class PrivateChannelResponse {
	
	/**
	 * The last message sent on this channel
	 */
	public String last_message_id;
	
	/**
	 * The recipient of messages on this channel
	 */
	public UserResponse recipient;
	
	/**
	 * The channel id
	 */
	public String id;
	
	/**
	 * This should always be true
	 */
	public boolean is_private;
}
