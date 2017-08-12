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

package sx.blah.discord.handle.impl.events.guild;

import sx.blah.discord.handle.obj.IGuild;

/**
 * Dispatched when a guild is received from Discord. This can happen in a number of scenarios:
 * <ul>
 *     <li>When connecting to the gateway.</li>
 *     <li>When the bot is added to a guild.</li>
 *     <li>When a previously-unavailable guild becomes available.</li>
 * </ul>
 */
public class GuildCreateEvent extends GuildEvent {

	public GuildCreateEvent(IGuild guild) {
		super(guild);
	}
}
