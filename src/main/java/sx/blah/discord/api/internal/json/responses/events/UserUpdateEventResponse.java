package sx.blah.discord.api.internal.json.responses.events;

import sx.blah.discord.api.internal.json.responses.UserResponse;

/**
 * This is received when a user updates his/her account info
 */
public class UserUpdateEventResponse extends UserResponse {

	/**
	 * Whether the user is verified
	 */
	public boolean verified;

	/**
	 * The user's email
	 */
	public String email;
}
