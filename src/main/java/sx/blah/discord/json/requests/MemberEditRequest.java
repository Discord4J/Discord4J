package sx.blah.discord.json.requests;

import sx.blah.discord.handle.obj.IRole;

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

	public MemberEditRequest(IRole[] roles) {
		this.roles = new String[roles.length];
		for (int i = 0; i < roles.length; i++)
			this.roles[i] = roles[i].getID();
	}
}
