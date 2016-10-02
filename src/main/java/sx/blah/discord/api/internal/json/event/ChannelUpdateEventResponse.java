package sx.blah.discord.api.internal.json.event;


import sx.blah.discord.api.internal.json.objects.ChannelObject;

/**
 * This is received when a channel's information is updated.
 */
public class ChannelUpdateEventResponse extends ChannelObject {

	/**
	 * Whether this is a private channel or not.
	 */
	public boolean is_private;
}
