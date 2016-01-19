package sx.blah.discord.json.responses;

import sx.blah.discord.json.generic.PermissionOverwrite;

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
	public PermissionOverwrite[] permission_overwrites;
	
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
}
