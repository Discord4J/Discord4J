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

import java.awt.Color;
import java.util.EnumSet;

/**
 * A role in a {@link IGuild}.
 */
public interface IRole extends IDiscordObject<IRole> {

	/**
	 * Gets the position of the role. Lowest is @everyone at -1.
	 *
	 * @return The position of the role.
	 */
	int getPosition();

	/**
	 * Gets permissions granted to the role.
	 *
	 * @return The permissions granted to the role.
	 */
	EnumSet<Permissions> getPermissions();

	/**
	 * Gets the name of the role.
	 *
	 * @return The name of the role.
	 */
	String getName();

	/**
	 * Gets whether the role is managed by an external service like Twitch.
	 *
	 * @return Whether the role is managed by an external service.
	 */
	boolean isManaged();

	/**
	 * Gets whether the role is hoisted. (Displayed separately in the online user list)
	 *
	 * @return Whether the role is hoisted.
	 */
	boolean isHoisted();

	/**
	 * Gets the color of the role.
	 *
	 * @return The color of the role.
	 */
	Color getColor();

	/**
	 * Gets whether the role is mentionable.
	 *
	 * @return Whether the role is mentionable.
	 */
	boolean isMentionable();

	/**
	 * Gets the parent guild of the role.
	 *
	 * @return The parent guild of the role.
	 */
	IGuild getGuild();

	/**
	 * Edits all properties of the role.
	 *
	 * @param color The color of the role.
	 * @param hoist Whether to hoist the role.
	 * @param name The name of the role.
	 * @param permissions The permissions the role grants.
	 * @param isMentionable Whether the role is mentionable.
	 */
	void edit(Color color, boolean hoist, String name, EnumSet<Permissions> permissions, boolean isMentionable);

	/**
	 * Changes the color of the role.
	 *
	 * @param color The color of the role.
	 */
	void changeColor(Color color);

	/**
	 * Changes whether to hoist the role.
	 *
	 * @param hoist Whether to hoist the role.
	 */
	void changeHoist(boolean hoist);

	/**
	 * Changes the name of the role.
	 *
	 * @param name The name of the role.
	 */
	void changeName(String name);

	/**
	 * Changes the permissions the role grants.
	 *
	 * @param permissions The permissions the role grants.
	 */
	void changePermissions(EnumSet<Permissions> permissions);

	/**
	 * Changes whether the role is mentionable.
	 *
	 * @param isMentionable Whether the role is mentionable.
	 */
	void changeMentionable(boolean isMentionable);

	/**
	 * Deletes the role.
	 */
	void delete();

	/**
	 * Gets whether the role is the @everyone role of the guild.
	 *
	 * @return Whether the role is the @everyone role of the guild.
	 */
	boolean isEveryoneRole();

	/**
	 * Gets whether the role is deleted.
	 *
	 * @return Whether the role is deleted.
	 */
	boolean isDeleted();

	/**
	 * Gets a formatted string mentioning the role.
	 *
	 * @return A formatted string mentioning the role.
	 */
	String mention();
}
