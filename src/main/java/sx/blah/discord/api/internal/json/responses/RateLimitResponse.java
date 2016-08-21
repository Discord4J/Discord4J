package sx.blah.discord.api.internal.json.responses;

/**
 * This represents the response sent by Discord in the event that an action has been rate limited.
 */
public class RateLimitResponse {

	/**
	 * Whether the bot has been globally rate limited.
	 */
	public boolean global;

	/**
	 * The amount of time (in milliseconds) before this client can send another request with the same bucket,
	 */
	public long retry_after;

	/**
	 * The message returned by Discord.
	 */
	public String message;
}
