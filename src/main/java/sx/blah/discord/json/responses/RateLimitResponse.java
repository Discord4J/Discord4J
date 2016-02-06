package sx.blah.discord.json.responses;

/**
 * This represents the response sent by Discord in the event that an action has been rate limited.
 */
public class RateLimitResponse {
	
	/**
	 * The type of action rate limited.
	 */
	public String bucket;
	
	/**
	 * The amount of time (in milliseconds) before this client can send another request with the same bucket,
	 */
	public long retry_after;
	
	/**
	 * The message returned by Discord.
	 */
	public String message;
}
