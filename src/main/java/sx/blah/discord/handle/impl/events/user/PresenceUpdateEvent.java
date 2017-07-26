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

package sx.blah.discord.handle.impl.events.user;

import sx.blah.discord.handle.obj.IPresence;
import sx.blah.discord.handle.obj.IUser;

/**
 * Dispatched when a user's presence changes.
 */
public class PresenceUpdateEvent extends UserEvent {

	private final IPresence oldPresence, newPresence;

	public PresenceUpdateEvent(IUser user, IPresence oldPresence, IPresence newPresence) {
		super(user);
		this.oldPresence = oldPresence;
		this.newPresence = newPresence;
	}

	/**
	 * Gets the user's new presence.
	 *
	 * @return The user's new presence.
	 */
	public IPresence getNewPresence() {
		return newPresence;
	}

	/**
	 * Gets the user's old presence.
	 *
	 * @return The user's old presence.
	 */
	public IPresence getOldPresence() {
		return oldPresence;
	}
}
