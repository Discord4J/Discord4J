package sx.blah.discord.json;

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
	public GameResponse game;
	
	/**
	 * Represents a user
	 */
	public class UserResponse {
		
		/**
		 * The user's id
		 */
		public String id;
	}
	
	/**
	 * Represents a game
	 */
	public class GameResponse {
		
		/**
		 * The game's name
		 */
		public String name;
	}
}
