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

import java.util.List;

/**
 * A voice channel in a {@link IGuild}.
 */
public interface IVoiceChannel extends IChannel {
	/**
	 * Gets the maximum number of users allowed in the voice channel at once. <code>0</code> indicates no limit.
	 *
	 * @return The maximum number of users allowed in the voice channel at once.
	 */
	int getUserLimit();

	/**
	 * Gets the bitrate of the voice channel (in bits).
	 *
	 * @return The bitrate of the voice channel.
     */
	int getBitrate();

	/**
	 * Edits all properties of the voice channel.
	 *
	 * @param name The name of the channel.
	 * @param position The position of the channel.
	 * @param bitrate The bitrate of the channel (in bits).
	 * @param userLimit The user limit of the channel.
	 */
	void edit(String name, int position, int bitrate, int userLimit);

	/**
	 * Changes the bitrate of the channel.
	 *
	 * @param bitrate The bitrate of the channel (in bits).
	 */
	void changeBitrate(int bitrate);

	/**
	 * Changes the user limit of the channel.
	 *
	 * @param limit The user limit of the channel.
	 */
	void changeUserLimit(int limit);

	/**
	 * Makes the bot user join the voice channel.
	 */
	void join();

	/**
	 * Makes the bot user leave the voice channel.
	 */
	void leave();

	/**
	 * Gets whether the bot user is connected to the voice channel.
	 *
	 * @return Whether the bot user is connected to the voice channel.
	 */
	boolean isConnected();

	/**
	 * {@inheritDoc}
	 */
	IVoiceChannel copy();

	/**
	 * Gets the users who are connected to the voice channel.
	 *
	 * @return The users who are connected to the voice channel.
	 */
	List<IUser> getConnectedUsers();
}
