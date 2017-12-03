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
 * An user's presence. This consists of a status, optional text, an optional streaming URL, and an optional activity.
 */
public interface IPresence {

	/**
	 * Gets the text for this presence.
	 *
	 * @return The text for this presence.
	 */
	Optional<String> getText();

	/**
	 * Gets the streaming URL.
	 *
	 * @return The streaming URL.
	 */
	Optional<String> getStreamingUrl();

	/**
	 * Gets the status.
	 *
	 * @return The status.
	 */
	StatusType getStatus();

	/**
	 * Creates a copy of the object.
	 *
	 * @return A copy of the object.
	 */
	IPresence copy();

	/**
	 * Gets the activity.
	 *
	 * @return The activity.
	 */
	Optional<ActivityType> getActivity();
}
