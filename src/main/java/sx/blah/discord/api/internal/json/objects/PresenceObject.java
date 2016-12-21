package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json presence object.
 */
public class PresenceObject {
	/**
	 * The user associated with this presence.
	 */
	public UserObject user;
	/**
	 * The status of the presence.
	 */
	public String status;
	/**
	 * The roles the user has.
	 */
	public RoleObject[] roles;
	/**
	 * The nickname of the user.
	 */
	public String nick;
	/**
	 * The guild id of the presence.
	 */
	public String guild_id;
	/**
	 * The game of the presence.
	 */
	public GameObject game;
}
