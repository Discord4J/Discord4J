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
 * Represents a user's presence - the online status (online, streaming, idle, etc.) and their playing text (along with
 * a streaming URL if they're in STREAMING mode).
 */
public interface IPresence {

	/**
	 * Returns the optional playing text. This is shown as the text after the <i>Playing</i> text.
	 *
	 * @return The playing text
	 */
	Optional<String> getPlayingText();

	/**
	 * Returns the optional streaming URL, which is used when a user is in the
	 * {@link StatusType#STREAMING STREAMING} {@link StatusType status type}.
	 *
	 * @return The streaming URL
	 */
	Optional<String> getStreamingUrl();

	/**
	 * Returns the online status of the user, which is the ONLINE or IDLE indicators.
	 *
	 * @return The online status
	 */
	StatusType getStatus();

	/**
	 * Create a copy of this object.
	 *
	 * @return A copy of this object
	 */
	IPresence copy();

}
