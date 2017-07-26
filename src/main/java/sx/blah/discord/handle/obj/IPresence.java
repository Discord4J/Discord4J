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

import java.util.Optional;

/**
 * A user's presence. This consists of an online status, an optional playing text, and an optional streaming URL.
 */
public interface IPresence {

	/**
	 * Gets the playing text. This is the text shown after <i>Playing</i> in the Discord client.
	 *
	 * @return The playing text.
	 */
	Optional<String> getPlayingText();

	/**
	 * Gets the streaming URL.
	 *
	 * @return The streaming URL.
	 */
	Optional<String> getStreamingUrl();

	/**
	 * Gets the online status.
	 *
	 * @return The online status.
	 */
	StatusType getStatus();

	/**
	 * Creates a copy of the object.
	 *
	 * @return A copy of the object.
	 */
	IPresence copy();

}
