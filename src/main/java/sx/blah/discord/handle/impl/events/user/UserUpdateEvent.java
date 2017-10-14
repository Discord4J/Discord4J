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

import sx.blah.discord.handle.obj.IUser;

/**
 * Dispatched when a user is updated.
 */
public class UserUpdateEvent extends UserEvent {

	private IUser oldUser, newUser;

	public UserUpdateEvent(IUser oldUser, IUser newUser) {
		super(newUser);
		this.oldUser = oldUser;
		this.newUser = newUser;
	}

	/**
	 * Gets the user before it was updated.
	 *
	 * @return The user before it was updated.
	 */
	public IUser getOldUser() {
		return oldUser;
	}

	/**
	 * Gets the user after it was updated.
	 *
	 * @return The user after it was updated.
	 */
	public IUser getNewUser() {
		return newUser;
	}
}
