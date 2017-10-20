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

import sx.blah.discord.handle.obj.IEmbed;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Dispatched when a message is created with embeds or a message is updated to have embeds.
 * <p>
 * NOTE: {@link #getOldMessage()} will not return null if and only if this event is related to editing a message
 * <b>and</b> the previous message was in the cache.
 */
public class MessageEmbedEvent extends MessageUpdateEvent {

	private final List<IEmbed> newEmbeds;

	public MessageEmbedEvent(IMessage oldMessage, IMessage newMessage, List<IEmbed> oldEmbeds) {
		super(oldMessage, newMessage);

		List<IEmbed> tempArray = new ArrayList<>();
		for (IEmbed attachment : newMessage.getEmbeds()) {
			if (!oldEmbeds.contains(attachment)) {
				tempArray.add(attachment);
			}
		}
		newEmbeds = tempArray;
	}

	/**
	 * Gets the new embeds that have been added to the message.
	 *
	 * @return The new embeds that have been added to the message.
	 */
	public List<IEmbed> getNewEmbeds() {
		return newEmbeds;
	}
}
