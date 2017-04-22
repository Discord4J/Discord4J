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

import sx.blah.discord.handle.impl.events.user.UserEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Status;

/**
 * This event was dispatched when a user's status changed.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent} instead.
 */
@Deprecated
public class StatusChangeEvent extends UserEvent {

	private final Status oldStatus, newStatus;

	public StatusChangeEvent(IUser user, Status oldStatus, Status newStatus) {
		super(user);
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
	}

	/**
	 * Gets the new status.
	 *
	 * @return The new status.
	 */
	public Status getNewStatus() {
		return newStatus;
	}

	/**
	 * Gets the old status.
	 *
	 * @return The old status.
	 */
	public Status getOldStatus() {
		return oldStatus;
	}

}
