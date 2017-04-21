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
 * This event is dispatched when a guild becomes unavailable.
 * Note: this guild is removed from the guild list when this happens!
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
	 * Gets the guild that became unavailable.
	 *
	 * @return The guild. This will not be present if a guild was never initialized before the ready event.
	 */
	public Optional<IGuild> getOptionalGuild() {
		return Optional.ofNullable(getGuild());
	}

	/**
	 * Gets the id of the guild that became unavailable. This is always available.
	 *
	 * @return The unavailable guild.
	 * @deprecated Use {@link #getGuildLongID()} instead
	 */
	@Deprecated
	public String getGuildID() {
		return Long.toUnsignedString(id);
	}

	/**
	 * Gets the id of the guild that became unavailable. This is always available.
	 *
	 * @return The unavailable guild.
	 */
	public long getGuildLongID() {
		return id;
	}
}
