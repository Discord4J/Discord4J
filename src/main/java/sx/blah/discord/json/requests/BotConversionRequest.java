package sx.blah.discord.json.requests;

/**
 * This is sent to convert a user account to a bot account.
 */
public class BotConversionRequest {

	/**
	 * The user's token.
	 */
	public String token;

	public BotConversionRequest(String token) {
		this.token = token;
	}
}
