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
 * Represents a verification level for a guild
 */
public enum VerificationLevel {
	/**
	 * Represents a verification level of None
	 */
	NONE,
	/**
	 * Represents a verification level of Low
	 */
	LOW,
	/**
	 * Represents a verification level of Medium
	 */
	MEDIUM,
	/**
	 * Represents a verification level of (╯°□°）╯︵ ┻━┻
	 */
	HIGH,
	/**
	 * Represents a verification level of ┻━┻ ﾐヽ(ಠ益ಠ)ノ彡┻━┻
	 */
	EXTREME,
	/**
	 * Unknown verification level
	 */
	UNKNOWN;

	public static VerificationLevel get(int id) {
		if (id >= values().length) {
			return UNKNOWN;
		} else {
			return values()[id];
		}
	}
}
