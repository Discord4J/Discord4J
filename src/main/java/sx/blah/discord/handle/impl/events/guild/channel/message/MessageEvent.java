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
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**
 * A generic message-related event.
 */
public abstract class MessageEvent extends ChannelEvent {

	private final IMessage message;
	private final long messageID;

	public MessageEvent(IMessage message) {
		super(message.getChannel());
		this.message = message;
		this.messageID = message.getLongID();
	}

	public MessageEvent(IChannel channel, long messageID) {
		super(channel);
		this.message = null;
		this.messageID = messageID;
	}

	/**
	 * Gets the message involved in the event.
	 *
	 * @return The message involved.
	 */
	public IMessage getMessage() {
		return message;
	}

	/**
	 * Gets the author of the message.
	 *
	 * <p>This is equivalent to <code>getMessage().getAuthor()</code>
	 *
	 * @return The author of the message.
	 */
	public IUser getAuthor() {
		return message == null ? null : message.getAuthor();
	}

	/**
	 * Gets the ID of the message involved in the event. This is always present.
	 *
	 * @return The ID of the message involved in the event.
	 */
	public long getMessageID() {
		return messageID;
	}
}
