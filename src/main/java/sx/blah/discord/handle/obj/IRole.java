package sx.blah.discord.handle.obj;

import java.awt.*;
import java.util.EnumSet;

/**
 * Represents a role.
 */
public interface IRole {
	
	/**
	 * Gets the position of the role, the higher the number the higher priority it has on sorting. @everyone is always -1
	 * 
	 * @return The position.
	 */
	int getPosition();
	
	/**
	 * Gets the position the role allows.
	 * 
	 * @return The set of enabled permissions.
	 */
	EnumSet<Permissions> getPermissions();
	
	/**
	 * Gets the name of the role.
	 * 
	 * @return The name.
	 */
	String getName();
	
	/**
	 * Checks whether the role is managed by an external plugin like twitch.
	 * 
	 * @return True if managed, false if otherwise.
	 */
	boolean isManaged();
	
	/**
	 * Gets the unique id of the role.
	 * 
	 * @return The role id.
	 */
	String getId();
	
	/**
	 * Gets whether the role is hoisted–meaning that it is displayed separately from the @everyone role.
	 * 
	 * @return True if hoisted, false if otherwise.
	 */
	boolean isHoisted();
	
	/**
	 * Gets the color for this role.
	 * 
	 * @return The color.
	 */
	Color getColor();
}
