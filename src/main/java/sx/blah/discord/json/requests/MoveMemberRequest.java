package sx.blah.discord.json.requests;

/**
 * This is sent to request that a user is moved to a new voice channel.
 */
public class MoveMemberRequest {

	/**
	 * The new voice channel for the user to be moved to.
	 */
	public String channel_id;

	public MoveMemberRequest(String channel_id) {
		this.channel_id = channel_id;
	}
}
