package sx.blah.discord.json.requests;

/**
 * This request is sent to modify a user's roles.
 */
public class MemberEditRequest {
	
	/**
	 * Roles for the user to have.
	 */
	public String[] roles;
	
	public MemberEditRequest(String[] roles) {
		this.roles = roles;
	}
}
