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

/**
 * The online statuses a user can have.
 */
public enum StatusType {

	/**
	 * A status of Online.
	 */
	ONLINE,
	/**
	 * A status of Do Not Disturb.
	 */
	DND,
	/**
	 * A status of AFK.
	 */
	IDLE,
	/**
	 * A status of Invisible.
	 *
	 * <p>Note: Another user will never appear to have this status, as Discord does not send this information. Instead,
	 * they will appear to be {@link #OFFLINE}. This status is used only for sending the bot's own invisible status to
	 * Discord.
	 */
	INVISIBLE,
	/**
	 * A status of Offline.
	 */
	OFFLINE,
	/**
	 * An unknown presence.
	 */
	UNKNOWN;

	/**
	 * Gets a status by its name.
	 *
	 * @param name The name of the status to get.
	 * @return The corresponding status or {@link #UNKNOWN} if the name did not match any known status.
	 */
	public static StatusType get(String name) {
		try {
			return valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return UNKNOWN;
		}
	}

}
