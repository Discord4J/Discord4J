package sx.blah.discord.json.requests;

/**
 * This is used to request a private channel be created with a user
 */
public class PrivateChannelRequest {
	
	/**
	 * The user id of the user the channel directed towards
	 */
	public String recipient_id;
	
	public PrivateChannelRequest(String recipient_id) {
		this.recipient_id = recipient_id;
	}
}
