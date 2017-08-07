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
 * The online statuses a user can be, such as ONLINE or IDLE.
 */
public enum StatusType {

	/**
	 * Represents that the user is online.
	 */
	ONLINE,
	/**
	 * Represents that the user is in 'do not disturb' mode.
	 */
	DND,
	/**
	 * Represents that the user is idle.
	 */
	IDLE,
	/**
	 * Represents that a user is invisible. Note: Another user will never appear to have this status, as Discord does
	 * not send this information. Instead, they will appear to be {@link #OFFLINE}. This status is used only for sending
	 * the bot's own status to Discord.
	 */
	INVISIBLE,
	/**
	 * Represents that the user is offline.
	 */
	OFFLINE,
	/**
	 * Represents that the user is streaming.
	 *
	 * @deprecated There is no such thing as streaming status. A user will <b>never</b> appear to have this status.
	 * Instead, they will have {@link #ONLINE} status with a streaming url.
	 */
	@Deprecated
	STREAMING,
	/**
	 * Unknown presence.
	 */
	UNKNOWN;

	public static StatusType get(String name) {
		try {
			return valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return UNKNOWN;
		}
	}

}
