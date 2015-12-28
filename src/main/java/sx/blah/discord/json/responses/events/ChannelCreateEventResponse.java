package sx.blah.discord.json.responses.events;

import sx.blah.discord.json.responses.ChannelResponse;

/**
 * This is received when a channel is created
 */
public class ChannelCreateEventResponse {
	
	/**
	 * The channel type, either "text" or "voice"
	 */
	public String type;
	
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
	public ChannelResponse.PermissionOverwriteResponse[] permission_overwrites;
	
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
	
	/**
	 * The guild the channel belongs to
	 */
	public String guild_id;
}
