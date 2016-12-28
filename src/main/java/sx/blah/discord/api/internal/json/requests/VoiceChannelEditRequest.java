package sx.blah.discord.api.internal.json.requests;

public class VoiceChannelEditRequest {

	/**
	 * The new name (2-100 characters long) of the channel.
	 */
	public String name;

	/**
	 * The new position of the channel.
	 */
	public int position;

	/**
	 * The new bitrate of the channel.
	 */
	public int bitrate;

	/**
	 * The new user limit of the channel.
	 */
	public int user_limit;

	public VoiceChannelEditRequest(String name, int position, int bitrate, int user_limit) {
		this.name = name;
		this.position = position;
		this.bitrate = bitrate;
		this.user_limit = user_limit;
	}
}
