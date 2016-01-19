package sx.blah.discord.api;

/**
 * This represents an exception thrown when there is an error doing a discord operation.
 */
public class DiscordException extends Exception {
	
	/**
	 * @param message The error message
	 */
	public DiscordException(String message) {
		super(message);
	}
}
