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

import discord4j.core.trait.Nameable;

/** A Discord voice region. */
public interface Region extends Identifiable<String>, Nameable {

	/**
	 * Gets an example hostname for the region.
	 *
	 * @return An example hostname for the region.
	 */
	String getSampleHostname();

	/**
	 * Gets an example port for the region.
	 *
	 * @return An example port for the region.
	 */
	int getSamplePort();

	/**
	 * Gets if this is a VIP region.
	 *
	 * @return {@code true} if this is a VIP region, {@code false} otherwise.
	 */
	boolean isVip();

	/**
	 * Gets if the region is closest to the current user's client.
	 *
	 * @return {@code true} if the region is closest to the current user's client, {@code false} otherwise.
	 */
	boolean isOptimal();

	/**
	 * Gets if this is a deprecated voice region.
	 *
	 * @return {@code true} if this is a deprecated voice region, {@code false} otherwise.
	 */
	boolean isDeprecated();

	/**
	 * Gets if this is a custom voice region.
	 *
	 * @return {@code true} if this is a custom voice region, {@code false} otherwise.
	 */
	boolean isCustom();
}
