package sx.blah.discord.api.internal.json.requests;

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

	/**
	 * Whether the role is mentionable.
	 */
	public boolean mentionable;

	public RoleEditRequest(Color color, boolean hoist, String name, EnumSet<Permissions> permissions, boolean mentionable) {
		this.color = color.getRGB() & 0x00ffffff; // & 0x00ffffff eliminates the alpha value
		this.hoist = hoist;
		this.name = name;
		this.permissions = Permissions.generatePermissionsNumber(permissions);
		this.mentionable = mentionable;
	}
}
