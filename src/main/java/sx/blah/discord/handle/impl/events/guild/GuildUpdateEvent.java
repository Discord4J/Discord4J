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
 * Dispatched when a guild is updated.
 */
public class GuildUpdateEvent extends GuildEvent {

	private final IGuild oldGuild, newGuild;

	public GuildUpdateEvent(IGuild oldGuild, IGuild newGuild) {
		super(newGuild);
		this.oldGuild = oldGuild;
		this.newGuild = newGuild;
	}

	/**
	 * Gets the guild before it was updated.
	 *
	 * @return The guild before it was updated.
	 */
	public IGuild getOldGuild() {
		return oldGuild;
	}

	/**
	 * Gets the guild after it was updated.
	 *
	 * @return The guild after it was updated.
	 */
	public IGuild getNewGuild() {
		return newGuild;
	}
}
