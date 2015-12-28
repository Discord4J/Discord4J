package sx.blah.discord.json.responses;

/**
 * A response received when changing account info
 */
public class AccountInfoChangeResponse {
	
	/**
	 * The new username
	 */
	public String username;
	
	/**
	 * Whether the user has been verified
	 */
	public boolean verified;
	
	/**
	 * The user's id
	 */
	public String id;
	
	/**
	 * The new login token
	 */
	public String token;
	
	/**
	 * The new avatar id
	 */
	public String avatar;
	
	/**
	 * The user's new discriminator
	 */
	public String discriminator;
	
	/**
	 * The user's new email
	 */
	public String email;
}
