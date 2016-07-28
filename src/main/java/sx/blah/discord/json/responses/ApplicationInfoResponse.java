package sx.blah.discord.json.responses;

/**
 * This is received in response to a request to discord regarding the bot's application information.
 */
public class ApplicationInfoResponse {

	/**
	 * The application's description.
	 */
	public String description;

	/**
	 * The application's icon.
	 */
	public String icon;

	/**
	 * The application's client id.
	 */
	public String id;

	/**
	 * The application's name.
	 */
	public String name;
	
	/**
	 * The application's owner.
	 */
	public UserResponse owner;
}
