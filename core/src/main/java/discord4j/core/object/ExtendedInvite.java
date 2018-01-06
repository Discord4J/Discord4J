/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object;

import discord4j.core.object.entity.User;

import java.time.Instant;
import java.util.Optional;

/** A Discord invite with metadata. */
public interface ExtendedInvite extends Invite {

	/**
	 * Gets the user who created the invite.
	 *
	 * @return The user who created the invite.
	 */
	User getUser();

	/**
	 * Gets the number of times this invite has been used.
	 *
	 * @return The number of times this invite has been used.
	 */
	int getUses();

	/**
	 * Gets the max number of times this invite can be used.
	 *
	 * @return The max number of times this invite can be used.
	 */
	int getMaxUses();

	/**
	 * Gets the instant this invite expires, if possible.
	 *
	 * @return The instant this invite expires, if possible.
	 */
	Optional<Instant> getExpiration();

	/**
	 * Gets when this invite was created.
	 *
	 * @return When this invite was created.
	 */
	Instant getCreation();

	/**
	 * Gets whether this invite is revoked.
	 *
	 * @return {@code true} if this invite was revoked, {@code false} otherwise.
	 */
	boolean isRevoked();
}
