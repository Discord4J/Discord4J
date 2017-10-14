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

package sx.blah.discord.handle.impl.events.guild.role;

import sx.blah.discord.handle.obj.IRole;

/**
 * Dispatched when a role is updated.
 */
public class RoleUpdateEvent extends RoleEvent {

	private final IRole oldRole, newRole;

	public RoleUpdateEvent(IRole oldRole, IRole newRole) {
		super(newRole);
		this.oldRole = oldRole;
		this.newRole = newRole;
	}

	/**
	 * Gets the role before it was updated.
	 *
	 * @return The role before it was updated.
	 */
	public IRole getOldRole() {
		return oldRole;
	}

	/**
	 * Gets role after it was updated.
	 *
	 * @return The role after it was updated.
	 */
	public IRole getNewRole() {
		return newRole;
	}
}
