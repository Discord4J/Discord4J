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

import java.util.List;

/**
 * A <b>custom</b> emoji in a guild. This is <b>not</b> a Unicode emoji.
 */
public interface IEmoji extends IDiscordObject<IEmoji> {

	/**
	 * Gets the parent guild of the emoji.
	 *
	 * @return The parent guild of the emoji.
	 */
	IGuild getGuild();

	/**
	 * Gets the emoji's name.
	 *
	 * @return The emoji's name.
	 */
	String getName();

	/**
	 * Gets the roles which are allowed to use the emoji.
	 *
	 * @return The roles which are allowed to use the emoji.
	 */
	List<IRole> getRoles();

	/**
	 * Gets whether the emoji needs colons in chat.
	 *
	 * @return Whether the emoji needs colons in chat.
	 */
	boolean requiresColons();

	/**
	 * Gets whether the emoji is managed by an external service like Twitch.
	 *
	 * @return Whether the emoji is managed by an external service.
	 */
	boolean isManaged();

	/**
	 * Gets whether the emoji is deleted.
	 *
	 * @return Whether the emoji is deleted.
	 */
	boolean isDeleted();

	/**
	 * Gets the image URL for the emoji.
	 *
	 * @return The image URL for the emoji.
	 */
	String getImageUrl();

	/**
	 * Changes the roles of the emoji.
	 * Your bot must be whitelisted by Discord to use this feature.
	 *
	 * @param roles The roles for which this emoji will be whitelisted, if empty all roles will be allowed.
	 */
	void changeRoles(IRole[] roles);

	/**
	 * Changes the name of the emoji.
	 *
	 * @param name The name, <b>without colons</b> of length 2-32 characters only consisting of alphanumeric characters and underscores.
	 */
	void changeName(String name);

	/**
	 * Deletes the emoji.
	 */
	void deleteEmoji();

	/**
	 * Gets whether the emoji is animated.
	 *
	 * @return Whether the emoji is animated.
	 */
	boolean isAnimated();
}
