package sx.blah.discord.util;

/**
 * Represents a 429 TOO MANY REQUESTS return code from a url connection.
 * This happens if your bot exceeds the Discord api message rate limit.
 */
public class HTTP429Exception extends Exception {
	
	public HTTP429Exception(String cause) {
		super(cause);
	}
}
