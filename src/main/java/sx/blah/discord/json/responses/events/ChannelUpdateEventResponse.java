package sx.blah.discord.json.responses.events;

import sx.blah.discord.json.responses.ChannelResponse;

/**
 * This is received when a channel's information is updated.
 */
public class ChannelUpdateEventResponse extends ChannelResponse {
	
	/**
	 * Whether this is a private channel or not.
	 */
	public boolean is_private;
}
