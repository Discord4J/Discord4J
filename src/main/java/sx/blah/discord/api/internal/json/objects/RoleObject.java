package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json role object.
 */
public class RoleObject {
	/**
	 * The id of the role.
	 */
	public String id;
	/**
	 * The name of the role.
	 */
	public String name;
	/**
	 * The color of the role.
	 */
	public int color;
	/**
	 * Whether the role should be displayed separately in the online users list.
	 */
	public boolean hoist;
	/**
	 * The position of the role.
	 */
	public int position;
	/**
	 * The permissions granted by this role.
	 */
	public int permissions;
	/**
	 * Whether the role is managed.
	 */
	public boolean managed;
	/**
	 * Whether the role is mentionable.
	 */
	public boolean mentionable;
}
