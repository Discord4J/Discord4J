package sx.blah.discord.json.requests;

/**
 * Object sent to login as a user
 */
public class LoginRequest {
	
	/**
	 * The user's email
	 */
	public String email;
	
	/**
	 * The user's password
	 */
	public String password;
	
	public LoginRequest(String email, String password) {
		this.email = email;
		this.password = password;
	}
}
