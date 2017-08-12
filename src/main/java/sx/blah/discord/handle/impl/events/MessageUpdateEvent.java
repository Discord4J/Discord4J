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

import sx.blah.discord.handle.obj.IMessage;

/**
 * Dispatched when a message's content is edited.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.message.MessageUpdateEvent} instead.
 */
@Deprecated
public class MessageUpdateEvent extends sx.blah.discord.handle.impl.events.guild.channel.message.MessageUpdateEvent {

	public MessageUpdateEvent(IMessage oldMessage, IMessage newMessage) {
		super(oldMessage, newMessage);
	}
}
