package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json user object.
 */
public class UserObject {
	/**
	 * The username of the user.
	 */
	public String username;
	/**
	 * The discriminator of the user.
	 */
	public String discriminator;
	/**
	 * The id of the user.
	 */
	public String id;
	/**
	 * The avatar of the user.
	 */
	public String avatar;
	/**
	 * Whether the user is a bot.
	 */
	public boolean bot = false;
}
