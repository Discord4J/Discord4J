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

import com.vdurmont.emoji.Emoji;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;

import java.util.List;

/**
 * Represents a single emoji with the users that reacted.
 */
public interface IReaction {

	/**
	 * Gets the message for this reaction.
	 *
	 * @return The message object
	 */
	IMessage getMessage();

	/**
	 * Whether or not this reaction is a custom emoji.
	 *
	 * @return If this is a custom emoji
	 */
	boolean isCustomEmoji();

	/**
	 * The IEmoji object if this is a custom emoji reaction, or null otherwise
	 *
	 * @return The IEmoji object or null if it's not a custom emoji
	 */
	IEmoji getCustomEmoji();

	/**
	 * The emoji-java Emoji object if this is a Unicode emoji reaction, or null otherwise
	 *
	 * @return The Emoji object or null if it's not a Unicode emoji
	 */
	Emoji getUnicodeEmoji();

	/**
	 * The amount of people that reacted.
	 *
	 * @return The amount of people that reacted
	 */
	int getCount();

	/**
	 * Gets the users that reacted.
	 *
	 * @return A list of users that reacted
	 */
	List<IUser> getUsers();

	/**
	 * Gets the {@link IDiscordClient} instance this object belongs to.
	 *
	 * @return The client instance.
	 */
	IDiscordClient getClient();

	/**
	 * Get the {@link IShard} instance this object belongs to.
	 */
	IShard getShard();

	/**
	 * Creates a new instance of this object with all the current properties.
	 *
	 * @return The copied instance of this object.
	 */
	IReaction copy();

	/**
	 * Gets a string representation of the emoji. Either the actual emoji, or the IEmoji#toString.
	 *
	 * @return The emoji, either as the emoji itself or the IEmoji formatted string
	 */
	String toString();

	/**
	 * Returns true if the given user reacted to the emoji. You may need to call refreshUsers first.
	 *
	 * @param user The user
	 * @return True if the user reacted, false otherwise
	 */
	boolean getUserReacted(IUser user);

	/**
	 * Returns true if this client's user reacted to the emoji. You may need to call refreshUsers first.
	 *
	 * @return True if we reacted, false otherwise
	 */
	boolean getClientReacted();

}
