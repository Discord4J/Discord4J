package sx.blah.discord.json.responses.events;

/**
 * This is received when a user updates his/her account info
 */
public class UserUpdateEventResponse {
	
	/**
	 * Whether the user is verified
	 */
	public boolean verified;
	
	/**
	 * The user's username
	 */
	public String username;
	
	/**
	 * Used to differentiate between two users with the same username
	 */
	public String discriminator;
	
	/**
	 * The user's id
	 */
	public String id;
	
	/**
	 * The user's avatar id, or null if no avatar is present
	 */
	public String avatar;
	
	/**
	 * The user's email
	 */
	public String email;
}
