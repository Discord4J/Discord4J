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
package sx.blah.discord.api.events;

/**
 * Represents the event execution order priority, the lower the priority is, the less important it
 * becomes and the later will be executed.
 */
public enum EventPriority {

	// Please keep in mind, the priority in our systems works based on the enum order, so take that in
	// consideration when adding new priorities.

	/**
	 * The highest priority, events with this priority will be ran first.
	 */
	HIGH,

	/**
	 * The default priority, neither high or low and will be ran normally.
	 */
	NORMAL,

	/**
	 * The lowest priority, events with this priority will be ran last.
	 */
	LOW,
}
