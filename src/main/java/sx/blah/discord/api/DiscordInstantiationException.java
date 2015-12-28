package sx.blah.discord.api;

/**
 * This represents an exception thrown by {@link ClientBuilder} when there is an error creating a discord instance
 */
public class DiscordInstantiationException extends Exception {
	
	/**
	 * @param message The error message
	 */
	public DiscordInstantiationException(String message) {
		super(message);
	}
}
