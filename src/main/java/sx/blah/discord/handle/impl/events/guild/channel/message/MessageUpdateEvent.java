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

import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever a message is edited.
 * NOTE: This event will be fired on pin/unpins and when embedded content is added when the message is
 * not present in Discord4J's cache. Additionally, in that case, {@link #getOldMessage()} will return
 * null.
 */
public class MessageUpdateEvent extends MessageEvent {

	private final IMessage oldMessage, newMessage;

	public MessageUpdateEvent(IMessage oldMessage, IMessage newMessage) {
		super(newMessage);
		this.oldMessage = oldMessage;
		this.newMessage = newMessage;
	}

	/**
	 * The original message.
	 *
	 * @return The message.
	 */
	public IMessage getOldMessage() {
		return oldMessage;
	}

	/**
	 * The new message.
	 *
	 * @return The message.
	 */
	public IMessage getNewMessage() {
		return newMessage;
	}
}
