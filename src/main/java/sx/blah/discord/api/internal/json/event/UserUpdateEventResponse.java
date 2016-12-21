package sx.blah.discord.api.internal.json.event;

import sx.blah.discord.api.internal.json.objects.UserObject;

/**
 * This is received when a user updates his/her account info
 */
public class UserUpdateEventResponse extends UserObject {

	/**
	 * Whether the user is verified
	 */
	public boolean verified;

	/**
	 * The user's email
	 */
	public String email;
}
