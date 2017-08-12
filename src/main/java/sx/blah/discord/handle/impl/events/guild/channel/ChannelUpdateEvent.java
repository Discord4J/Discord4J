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

import sx.blah.discord.handle.obj.IChannel;

/**
 * Dispatched when a channel is updated.
 */
public class ChannelUpdateEvent extends ChannelEvent {

	private final IChannel oldChannel, newChannel;

	public ChannelUpdateEvent(IChannel oldChannel, IChannel newChannel) {
		super(newChannel);
		this.oldChannel = oldChannel;
		this.newChannel = newChannel;
	}

	/**
	 * Gets the channel before it was updated.
	 *
	 * @return The channel before it was updated.
	 */
	public IChannel getOldChannel() {
		return oldChannel;
	}

	/**
	 * Gets the channel after it was updated.
	 *
	 * @return The channel after it was updated.
	 */
	public IChannel getNewChannel() {
		return newChannel;
	}
}
