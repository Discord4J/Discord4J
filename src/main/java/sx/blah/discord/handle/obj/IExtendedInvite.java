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

import java.time.Instant;

/**
 * An invite to a guild channel with extra metadata.
 */
public interface IExtendedInvite extends IInvite {

	/**
	 * Gets the number of times the invite has been used.
	 *
	 * @return The number of times the invite has been used.
	 */
	int getUses();

	/**
	 * Gets the maximum number of times the invite can be used. 0 indicates infinite uses.
	 *
	 * @return The maximum number of times the invite can be used.
	 */
	int getMaxUses();

	/**
	 * Gets the duration (in seconds) after which the invite expires.
	 *
	 * @return The duration (in seconds) after which the invite expires.
	 */
	int getMaxAge();

	/**
	 * Gets whether membership granted by the invite is temporary.
	 *
	 * @return Whether membership granted by the invite is temporary.
	 */
	boolean isTemporary();

	/**
	 * Gets the time at which the invite was created.
	 *
	 * @return The time at which the invite was created.
	 */
	Instant getCreationTime();

	/**
	 * Gets whether the invite is revoked.
	 *
	 * @return Whether the invite is revoked.
	 */
	boolean isRevoked();
}
