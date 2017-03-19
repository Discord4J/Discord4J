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

import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;

import java.util.List;

/**
 * Fired whenever emojis change in a guild.
 */
public class GuildEmojisUpdateEvent extends GuildEvent {

	private final List<IEmoji> oldEmojis;
	private final List<IEmoji> newEmojis;

	public GuildEmojisUpdateEvent(IGuild guild, List<IEmoji> old, List<IEmoji> brandNew){
		super(guild);
		oldEmojis = old;
		newEmojis = brandNew;
	}

	/**
	 * Gets the list of old emojis prior to the change.
	 *
	 * @return The old emojis.
	 */
	public List<IEmoji> getOldEmojis() {
		return oldEmojis;
	}

	/**
	 * Gets the new list of emojis.
	 *
	 * @return The old emojis.
	 */
	public List<IEmoji> getNewEmojis() {
		return newEmojis;
	}
}
