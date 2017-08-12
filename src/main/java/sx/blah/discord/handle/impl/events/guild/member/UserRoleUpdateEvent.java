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

package sx.blah.discord.handle.impl.events.guild.member;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

/**
 * Dispatched when a member's roles are updated.
 */
public class UserRoleUpdateEvent extends GuildMemberEvent {

	private final List<IRole> oldRoles, newRoles;

	public UserRoleUpdateEvent(IGuild guild, IUser user, List<IRole> oldRoles, List<IRole> newRoles) {
		super(guild, user);
		this.oldRoles = oldRoles;
		this.newRoles = newRoles;
	}

	/**
	 * Gets the old roles for the user.
	 *
	 * @return The old roles.
	 */
	public List<IRole> getOldRoles() {
		return oldRoles;
	}

	/**
	 * Gets the new roles for the user.
	 *
	 * @return The new roles.
	 */
	public List<IRole> getNewRoles() {
		return newRoles;
	}
}
