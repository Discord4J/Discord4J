package sx.blah.discord.json.responses.events;

/**
 * This is received when a channel's information is updated.
 */
public class ChannelUpdateEventResponse extends ChannelCreateEventResponse {
	
	/**
	 * Whether this is a private channel or not.
	 */
	public boolean is_private;
}
