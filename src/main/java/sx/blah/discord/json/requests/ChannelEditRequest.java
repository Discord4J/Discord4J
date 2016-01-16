package sx.blah.discord.json.requests;

/**
 * This is the request sent in order to edit a channel's information.
 */
public class ChannelEditRequest {
	
	/**
	 * The new name (2-100 characters long) of the channel.
	 */
	public String name;
	
	/**
	 * The new position of the channel.
	 */
	public int positon;
	
	/**
	 * The new topic of the channel.
	 */
	public String topic;
	
	public ChannelEditRequest(String name, int positon, String topic) {
		this.name = name;
		this.positon = positon;
		this.topic = topic;
	}
}
