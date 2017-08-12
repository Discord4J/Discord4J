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

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;

/**
 * Dispatched when a reaction gets removed from a message.
 */
public class ReactionRemoveEvent extends ReactionEvent {

	public ReactionRemoveEvent(IMessage message, IReaction reaction, IUser user) {
		super(message, reaction, user);
	}

	/**
	 * Gets the user who removed the reaction.
	 *
	 * @return The user who removed the reaction.
	 */
	@Override
	public IUser getUser() {
		return super.getUser();
	}
}
