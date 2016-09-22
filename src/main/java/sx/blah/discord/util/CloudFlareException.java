package sx.blah.discord.util;

/**
 * In case of a CloudFlare interruption this exception gets thrown
 * <br>
 * Created by Arsen on 22.9.16..
 */
public class CloudFlareException extends Exception {
	private String message;

	/**
	 * Creates a new CloudFlareException
	 *
	 * @param message The error message
	 */
	public CloudFlareException(String message) {
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
