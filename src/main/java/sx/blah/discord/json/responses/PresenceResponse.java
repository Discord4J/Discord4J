package sx.blah.discord.json.responses;

import sx.blah.discord.json.generic.StatusObject;

/**
 * Represents a user's presence
 */
public class PresenceResponse {

	/**
	 * The user this represents
	 */
	public UserResponse user;

	/**
	 * The status for the user, either: "idle" or "online"
	 */
	public String status;

	/**
	 * The game the user is playing (or null if no game being played)
	 */
	public StatusObject game;

	/**
	 * Represents a user
	 */
	public static class UserResponse {

		/**
		 * The user's id
		 */
		public String id;
	}
}
