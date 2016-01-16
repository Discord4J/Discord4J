package sx.blah.discord.json.requests;

/**
 * Represents a request to create a new channel.
 */
public class ChannelCreateRequest {
	
	/**
	 * The name of the channel. Must be 2-100 characters long
	 */
	public String name;
	
	/**
	 * The type of channel, either "text" or "voice"
	 */
	public String type;
	
	public ChannelCreateRequest(String name, String type) {
		this.name = name;
		this.type = type;
	}
}
