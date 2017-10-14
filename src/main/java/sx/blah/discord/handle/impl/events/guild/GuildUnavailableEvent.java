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

import java.util.Optional;

/**
 * Dispatched when a guild becomes unavailable.
 */
public class GuildUnavailableEvent extends GuildEvent {

	private final long id;

	public GuildUnavailableEvent(IGuild guild) {
		super(guild);
		this.id = guild.getLongID();
	}

	public GuildUnavailableEvent(long id) {
		super(null);
		this.id = id;
	}

	/**
	 * Gets the guild that became unavailable. This is not present if the guild was not previously cached.
	 *
	 * @return The guild that became unavailable.
	 */
	public Optional<IGuild> getOptionalGuild() {
		return Optional.ofNullable(getGuild());
	}

	/**
	 * Gets the ID of the guild that became unavailable.
	 *
	 * @return The ID of the guild that became unavailable.
	 */
	public long getGuildLongID() {
		return id;
	}
}
