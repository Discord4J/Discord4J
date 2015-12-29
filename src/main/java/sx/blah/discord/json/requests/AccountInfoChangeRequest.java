package sx.blah.discord.json.requests;

/**
 * This is sent when the account changes its info
 */
public class AccountInfoChangeRequest {
	
	/**
	 * The account's email, if this is different than the old email, the account email is changed
	 */
	public String email;
	/**
	 * The account's current password
	 */
	public String password;
	/**
	 * The account's new password, or null if it should stay the same
	 */
	public String new_password;
	/**
	 * The account's username, if this is different than the old username, the account's username is changed
	 */
	public String username;
	/**
	 * The account's avatar id, if this is different than the old username, the account's username is changed
	 */
	public String avatar;
	
	public AccountInfoChangeRequest(String email, String password, String new_password, String username, String avatar) {
		this.email = email;
		this.password = password;
		this.new_password = new_password;
		this.username = username;
		this.avatar = avatar;
	}
}
