package sx.blah.discord.json.generic;

/**
 * Represents a role
 */
public class RoleResponse {
	
	/**
	 * Where the role should be displayed. -1 is @everyone, it is always last
	 */
	public int position;
	
	/**
	 * The permissions the user has. See http://bit.ly/1OnxVZe. To see if the user has a permission,
	 * {@code 1 << [permOffset] & [permissions number] must be greater than 0. Remember to check the manage roles permission!}
	 * That permissions supercedes all others
	 */
	public int permissions;
	
	/**
	 * The role name
	 */
	public String name;
	
	/**
	 * Whether this role is managed via plugins like twitch
	 */
	public boolean managed;
	
	/**
	 * The role id
	 */
	public String id;
	
	/**
	 * Whether to display this role separately from others
	 */
	public boolean hoist;
	
	/**
	 * The DECIMAL format for the color
	 */
	public int color;
	
	public RoleResponse() {}
}
