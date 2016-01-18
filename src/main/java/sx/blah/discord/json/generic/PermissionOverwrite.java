package sx.blah.discord.json.generic;

/**
 * Represents a permissions overwrite json object.
 */
public class PermissionOverwrite {
	
	/**
	 * The permission type, either "role" or "member"
	 */
	public String type;
	
	/**
	 * Either the role or user id
	 */
	public String id;
	
	/**
	 * Permissions to deny, see {@link RoleResponse#permissions}
	 */
	public int deny;
	
	/**
	 * Permissions to allow, see {@link RoleResponse#permissions}
	 */
	public int allow;
	
	public PermissionOverwrite(String type, String id, int deny, int allow) {
		this.type = type;
		this.id = id;
		this.deny = deny;
		this.allow = allow;
	}
}
