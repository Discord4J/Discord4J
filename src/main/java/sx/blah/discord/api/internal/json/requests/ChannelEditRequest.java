package sx.blah.discord.api.internal.json.requests;

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
	public int position;

	/**
	 * The new topic of the channel.
	 */
	public String topic;

	/**
	 * The new bitrate of the channel. VOICE ONLY
	 */
	public int bitrate;

	/**
	 * The new user limit of the channel. VOICE ONLY
	 */
	public int user_limit;

	public ChannelEditRequest(String name, int position, String topic) {
		this.name = name;
		this.position = position;
		this.topic = topic;
		this.bitrate = 8000;
	}

	public ChannelEditRequest(String name, int position, int bitrate, int user_limit) {
		this.name = name;
		this.position = position;
		this.bitrate = bitrate;
		this.user_limit = user_limit;
	}
}
