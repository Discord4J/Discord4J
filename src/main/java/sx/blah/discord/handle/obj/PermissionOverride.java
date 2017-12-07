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
package sx.blah.discord.handle.obj;

import java.util.EnumSet;

/**
 * A permission override for a role or user.
 */
public class PermissionOverride implements IIDLinkedObject {

	/**
	 * The permissions explicitly allowed by the override.
	 */
	protected final EnumSet<Permissions> allow;

	/**
	 * The permissions explicitly denied by the override.
	 */
	protected final EnumSet<Permissions> deny;

	/**
	 * The ID of the user or role the override is for.
	 */
	protected final long id;

	public PermissionOverride(EnumSet<Permissions> allow, EnumSet<Permissions> deny, long id) {
		this.allow = allow;
		this.deny = deny;
		this.id = id;
	}

	/**
	 * Gets the permissions explicitly allowed by the override.
	 *
	 * @return The permissions explicitly allowed by the override.
	 */
	public EnumSet<Permissions> allow() {
		return allow;
	}

	/**
	 * Gets the permissions explicitly denied by the override.
	 *
	 * @return The permissions explicitly denied by the override.
	 */
	public EnumSet<Permissions> deny() {
		return deny;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;

		if (!this.getClass().isAssignableFrom(other.getClass()))
			return false;

		if (!((PermissionOverride) other).deny.equals(this.deny)) return false;
		if (!((PermissionOverride) other).allow.equals(this.allow)) return false;

		return true;
	}

	@Override
	public String toString() {
		return "PermissionOverride (Allow: " + allow + ", Deny: " + deny + ")";
	}

	@Override
	public long getLongID() {
		return id;
	}
}
