package sx.blah.discord.util;

/**
 * This represents an exception thrown when there is a miscellaneous error doing a discord operation.
 */
public class DiscordException extends Exception {

	private String message;

	/**
	 * @param message The error message
	 */
	public DiscordException(String message) {
		super(message);
		this.message = message;
	}

	/**
	 * This gets the error message sent by Discord.
	 *
	 * @return The error message.
	 */
	public String getErrorMessage() {
		return message;
	}
}
