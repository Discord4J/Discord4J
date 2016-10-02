/*
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.handle.obj;

import java.util.List;

public interface IEmoji extends IDiscordObject<IEmoji> {

	/**
	 * Copies this emoji object.
	 *
	 * @return A copy of this object.
	 */
	IEmoji copy();

	/**
	 * Gets the emoji's name.
	 *
	 * @return The name.
	 */
	String getName();

	/**
	 * Gets the guild for this emoji.
	 *
	 * @return The guild.
	 */
	IGuild getGuild();

	/**
	 * Returns true if the emoji needs colons, false otherwise.
	 *
	 * @return True if the emoji needs colons, false otherwise.
	 */
	boolean requiresColons();

	/**
	 * Checks whether the role is managed by an external plugin like Twitch.
	 *
	 * @return True if managed, false if otherwise.
	 */
	boolean isManaged();

	/**
	 * Gets the roles for this emoji. Possibly for integration, but unused at the moment.
	 *
	 * @return The roles list.
	 */
	List<IRole> getRoles();

	/**
	 * The emoji as a properly formatted string. "<:name:emoji_id>"
	 *
	 * @return The formatted string.
	 */
	@Override
	String toString();

}
