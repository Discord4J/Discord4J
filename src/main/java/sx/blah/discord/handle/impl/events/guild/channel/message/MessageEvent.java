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

package sx.blah.discord.handle.impl.events.guild.channel.message;

import sx.blah.discord.handle.impl.events.guild.channel.ChannelEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**
 * This represents a generic message event.
 */
public abstract class MessageEvent extends ChannelEvent {
	
	private final IMessage message;
	
	public MessageEvent(IMessage message) {
		super(message.getChannel());
		this.message = message;
	}
	
	/**
	 * This gets the message involved in this event.
	 *
	 * @return The message.
	 */
	public IMessage getMessage() {
		return message;
	}
	
	/**
	 * This gets the author of the message.
	 *
	 * @return The author.
	 */
	public IUser getAuthor() {
		return message.getAuthor();
	}
}
