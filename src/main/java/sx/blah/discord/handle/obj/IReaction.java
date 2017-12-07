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

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;

import java.util.List;

/**
 * A reaction on a {@link IMessage}.
 */
public interface IReaction {

	/**
	 * Gets the message the reaction is on.
	 *
	 * @return The message the reaction is on.
	 */
	IMessage getMessage();

	/**
	 * Gets the number of people who reacted.
	 *
	 * @return The number of people who reacted.
	 */
	int getCount();

	/**
	 * Gets a the emoji of the reaction.
	 *
	 * @return The emoji of the reaction.
	 */
	ReactionEmoji getEmoji();

	/**
	 * Gets the users who reacted with the same emoji.
	 *
	 * @return A list of users who reacted with the same emoji.
	 */
	List<IUser> getUsers();

	/**
	 * Gets whether the given user reacted with the same emoji.
	 *
	 * @param user The user.
	 * @return Whether the given user reacted with the same emoji.
	 */
	boolean getUserReacted(IUser user);

	/**
	 * Gets the client the object belongs to.
	 *
	 * @return The client the object belongs to.
	 */
	IDiscordClient getClient();

	/**
	 * Gets the shard the object belongs to.
	 *
	 * @return The shard the object belongs to.
	 */
	IShard getShard();

	/**
	 * Creates a new instance of the reaction with all the current properties.
	 *
	 * @return The copied instance of the reaction.
	 */
	IReaction copy();
}
