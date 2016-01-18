package sx.blah.discord.json.requests;

import sx.blah.discord.handle.obj.Permissions;

import java.awt.*;
import java.util.EnumSet;

/**
 * This is sent in order to edit a role.
 */
public class RoleEditRequest {
	
	/**
	 * The new color for the role.
	 */
	public int color;
	
	/**
	 * Whether to hoist the role.
	 */
	public boolean hoist;
	
	/**
	 * The new name of the role.
	 */
	public String name;
	
	/**
	 * The new permissions of the role.
	 */
	public int permissions;
	
	public RoleEditRequest(Color color, boolean hoist, String name, EnumSet<Permissions> permissions) {
		this.color = color.getRGB();
		this.hoist = hoist;
		this.name = name;
		this.permissions = Permissions.generatePermissionsNumber(permissions);
	}
}
