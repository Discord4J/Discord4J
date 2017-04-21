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

package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IGuild;

/**
 * This event is dispatched when a guild becomes unavailable.
 * Note: this guild is removed from the guild list when this happens!
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.GuildUnavailableEvent} instead.
 */
@Deprecated
public class GuildUnavailableEvent extends sx.blah.discord.handle.impl.events.guild.GuildUnavailableEvent {

	public GuildUnavailableEvent(IGuild guild) {
		super(guild);
	}

	public GuildUnavailableEvent(long id) {
		super(id);
	}
}
