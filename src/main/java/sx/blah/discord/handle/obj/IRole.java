/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.handle.obj;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

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
	 * Gets whether this role is mentionable or not.
	 *
	 * @return True if mentionable, false if otherwise.
	 */
	boolean isMentionable();

	/**
	 * Gets the guild this role belongs to.
	 *
	 * @return The guild.
	 */
	IGuild getGuild();

	/**
	 * Edits all properties of this role.
	 *
	 * @param color The new color of the role.
	 * @param hoist Whether the role should be displayed separately from others.
	 * @param name The new name of the role.
	 * @param permissions The new permissions set of the role.
	 * @param isMentionable Whether the role can be mentioned.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void edit(Color color, boolean hoist, String name, EnumSet<Permissions> permissions, boolean isMentionable);

	/**
	 * Changes the color of the role.
	 *
	 * @param color The new color for the role.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeColor(Color color);

	/**
	 * Changes whether to hoist the role.
	 *
	 * @param hoist Whether to hoist the role.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeHoist(boolean hoist);

	/**
	 * Changes the name of the role.
	 *
	 * @param name The new name for the role.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeName(String name);

	/**
	 * Changes the permissions of the role.
	 *
	 * @param permissions The new permissions for the role.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changePermissions(EnumSet<Permissions> permissions);

	/**
	 * Changes whether this role is mentionable.
	 *
	 * @param isMentionable Whether this role should be mentionable or not.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeMentionable(boolean isMentionable);

	/**
	 * Attempts to delete this role.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void delete();

	/**
	 * This checks if the role is the @everyone role.
	 *
	 * @return True if the @everyone role, false if otherwise.
	 */
	boolean isEveryoneRole();

	/**
	 * Checks to see if the this role is deleted.
	 *
	 * @return True if this role is deleted.
	 */
	boolean isDeleted();
	/**
	 * Formats a string to @mention the role.
	 *
	 * @return The formatted string. Note: if {@link #isMentionable()} returns false, this just returns the result of
	 * {@link #getName()}.
	 */
	String mention();
}
