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

import java.time.LocalDateTime;

/**
 * Represents an invite to a channel which extra metadata.
 */
public interface IExtendedInvite extends IInvite {

	/**
	 * @return The number of times this invite has been used.
	 */
	int getUses();

	/**
	 * @return The maximum number of times this invite can be used. 0 if infinite.
	 */
	int getMaxUses();

	/**
	 * @return The duration (in seconds) after which this invite expires.
	 */
	int getMaxAge();

	/**
	 * @return Whether this invite only grants temporary membership.
	 */
	boolean isTemporary();

	/**
	 * @return The time at which this invite was created.
	 */
	LocalDateTime getCreationTime();

	/**
	 * @return Whether this invite is revoked.
	 */
	boolean isRevoked();
}
