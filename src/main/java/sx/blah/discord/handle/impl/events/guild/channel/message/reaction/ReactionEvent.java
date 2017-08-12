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

package sx.blah.discord.handle.impl.events.guild.channel.message.reaction;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;

/**
 * A generic reaction-related event.
 */
public abstract class ReactionEvent extends MessageEvent {

	private final IReaction reaction;
	private final IUser user;

	public ReactionEvent(IMessage message, IReaction reaction, IUser user) {
		super(message);
		this.reaction = reaction;
		this.user = user;
	}

	/**
	 * Gets the reaction object for the event.
	 *
	 * @return The reaction object.
	 */
	public IReaction getReaction() {
		return reaction;
	}

	/**
	 * Gets the user involved in the event.
	 *
	 * @return The user involved.
	 */
	public IUser getUser() {
		return user;
	}

	/**
	 * Gets the number of users who have reacted with the same reaction.
	 *
	 * <p>This is equivalent to <code>getReaction().getCount()</code>
	 *
	 * @return The number of users who have reacted with the same reaction.
	 */
	public int getCount() {
		return reaction.getCount();
	}
}
