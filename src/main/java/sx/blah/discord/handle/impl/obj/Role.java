package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;

import java.awt.*;
import java.util.EnumSet;

public class Role implements IRole {
	
	/**
	 * Where the role should be displayed. -1 is @everyone, it is always last
	 */
	protected int position;
	
	/**
	 * The permissions the role has.
	 */
	protected EnumSet<Permissions> permissions;
	
	/**
	 * The role name
	 */
	protected String name;
	
	/**
	 * Whether this role is managed via plugins like twitch
	 */
	protected boolean managed;
	
	/**
	 * The role id
	 */
	protected String id;
	
	/**
	 * Whether to display this role separately from others
	 */
	protected boolean hoist;
	
	/**
	 * The DECIMAL format for the color
	 */
	protected Color color;
	
	public Role(int position, int permissions, String name, boolean managed, String id, boolean hoist, int color) {
		this.position = position;
		this.permissions = Permissions.getAllPermissionsForNumber(permissions);
		this.name = name;
		this.managed = managed;
		this.id = id;
		this.hoist = hoist;
		this.color = new Color(color);
	}
	
	@Override
	public int getPosition() {
		return position;
	}
	
	/**
	 * Sets the CACHED role position.
	 * 
	 * @param position The role position.
	 */
	public void setPosition(int position) {
		this.position = position;
	}
	
	@Override
	public EnumSet<Permissions> getPermissions() {
		return permissions.clone();
	}
	
	/**
	 * Sets the CACHED enabled permissions.
	 * 
	 * @param permissions The permissions number.
	 */
	public void setPermissions(int permissions) {
		this.permissions = Permissions.getAllPermissionsForNumber(permissions);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the CACHED role name.
	 * 
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean isManaged() {
		return managed;
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public boolean isHoisted() {
		return hoist;
	}
	
	/**
	 * Sets whether this role is hoisted in the CACHE.
	 * 
	 * @param hoist True if hoisted, false if otherwise.
	 */
	public void setHoist(boolean hoist) {
		this.hoist = hoist;
	}
	
	@Override
	public Color getColor() {
		return color;
	}
	
	/**
	 * Sets the CACHED role color.
	 * 
	 * @param color The color decimal number.
	 */
	public void setColor(int color) {
		this.color = new Color(color);
	}
}
