package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json ban object.
 */
public class BanObject {
	/**
	 * The reason for the ban.
	 * Note: Currently unused by Discord API.
	 */
	public String reason;
	/**
	 * The banned user.
	 */
	public UserObject user;
}
