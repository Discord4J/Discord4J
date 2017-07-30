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

package sx.blah.discord.handle.impl.events.guild.channel;

import sx.blah.discord.handle.impl.events.guild.GuildEvent;
import sx.blah.discord.handle.obj.IChannel;

/**
 * This represents a generic channel event.
 */
public abstract class ChannelEvent extends GuildEvent {

	private final IChannel channel;

	public ChannelEvent(IChannel channel) {
		super(channel.getGuild());
		this.channel = channel;
	}

	/**
	 * This gets the channel involved in this event.
	 *
	 * @return The channel.
	 */
	public IChannel getChannel() {
		return channel;
	}
}
