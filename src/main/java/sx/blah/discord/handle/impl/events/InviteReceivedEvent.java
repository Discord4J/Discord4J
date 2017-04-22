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

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IInvite;
import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched when a message the bot receives includes an invite link.
 *
 * @deprecated This event is marked for removal in future versions. Parsing invites on every received message is a
 * potentially very expensive operation. It also has very limited functionality for bot accounts. If it is necessary that
 * you receive invites on messages, use {@link sx.blah.discord.util.MessageTokenizer} in a {@link MessageReceivedEvent}
 * listener.
 */
@Deprecated
public class InviteReceivedEvent extends Event {

	private final IInvite[] invites;
	private final IMessage message;

	public InviteReceivedEvent(IInvite[] invites, IMessage message) {
		this.invites = invites;
		this.message = message;
	}

	/**
	 * Gets the invites received.
	 *
	 * @return The invites received.
	 */
	public IInvite[] getInvites() {
		return invites;
	}

	/**
	 * Gets the message which contains the invite.
	 *
	 * @return The message.
	 */
	public IMessage getMessage() {
		return message;
	}
}
