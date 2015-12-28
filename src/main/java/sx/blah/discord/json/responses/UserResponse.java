package sx.blah.discord.json.responses;

/**
 * A representation of a user
 */
public class UserResponse {
	
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
}
