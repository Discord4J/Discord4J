package sx.blah.discord.handle.obj;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

import java.awt.*;
import java.util.EnumSet;

/**
 * Represents a role.
 */
public interface IRole extends IDiscordObject<IRole> {

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

	/**
	 * Gets the guild this role belongs to.
	 *
	 * @return The guild.
	 */
	IGuild getGuild();

	/**
	 * Changes the color of the role.
	 *
	 * @param color The new color for the role.
	 *
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeColor(Color color) throws HTTP429Exception, DiscordException, MissingPermissionsException;

	/**
	 * Changes whether to hoist the role.
	 *
	 * @param hoist Whether to hoist the role.
	 *
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeHoist(boolean hoist) throws HTTP429Exception, DiscordException, MissingPermissionsException;

	/**
	 * Changes the name of the role.
	 *
	 * @param name The new name for the role.
	 *
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeName(String name) throws HTTP429Exception, DiscordException, MissingPermissionsException;

	/**
	 * Changes the permissions of the role.
	 *
	 * @param permissions The new permissions for the role.
	 *
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changePermissions(EnumSet<Permissions> permissions) throws HTTP429Exception, DiscordException, MissingPermissionsException;

	/**
	 * Attempts to delete this role.
	 *
	 * @throws MissingPermissionsException
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 */
	void delete() throws MissingPermissionsException, HTTP429Exception, DiscordException;
}
