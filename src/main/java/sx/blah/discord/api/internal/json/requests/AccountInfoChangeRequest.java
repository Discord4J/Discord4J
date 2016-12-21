package sx.blah.discord.api.internal.json.requests;

/**
 * This is sent when the account changes its info
 */
public class AccountInfoChangeRequest {

	/**
	 * The account's username, if this is different than the old username, the account's username is changed
	 */
	public String username;
	/**
	 * The account's avatar id, if this is different than the old username, the account's username is changed
	 */
	public String avatar;

	public AccountInfoChangeRequest(String username, String avatar) {
		this.username = username;
		this.avatar = avatar;
	}
}
