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
 * This represents a generic reaction event.
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
	 * Gets the reaction object.
	 *
	 * @return The reaction object.
	 */
	public IReaction getReaction() {
		return reaction;
	}
	
	/**
	 * Gets the user that did this action.
	 *
	 * @return The acting user
	 */
	public IUser getUser() {
		return user;
	}
	
	/**
	 * Gets the user count for this reaction.
	 *
	 * @return The user count
	 */
	public int getCount() {
		return reaction.getCount();
	}
}
