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
 * <b>DEPRECATED</b> - use {@link StatusType} instead.
 * <br><br>
 * Represents presences that a user can have.
 *
 * @deprecated Use {@link StatusType}
 */
@Deprecated
public enum Presences {
	/**
	 * Represents that the user is online.
	 */
	ONLINE,
	/**
	 * Represents that the user is idle.
	 */
	IDLE,
	/**
	 * Represents that the user is offline.
	 */
	OFFLINE,
	/**
	 * Represents that the user is streaming.
	 */
	STREAMING,
	/**
	 * Represents that the user is in 'do not disturb' mode.
	 */
	DND,
	/**
	 * Unknown presence.
	 */
	UNKNOWN;

	public static Presences get(String name) {
		try {
			return valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return UNKNOWN;
		}
	}
}
