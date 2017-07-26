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

package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.handle.obj.IRegion;

import java.util.Objects;

/**
 * The default implementation of {@link IRegion}.
 */
public class Region implements IRegion {

	/**
	 * The ID of the region.
	 */
	private final String id;
	/**
	 * The name of the region.
	 */
	private final String name;
	/**
	 * Whether the region is for VIP guilds.
	 */
	private final boolean vip;

	public Region(String id, String name, boolean vip) {
		this.id = id;
		this.name = name;
		this.vip = vip;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isVIPOnly() {
		return vip;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;

		return this.getClass().isAssignableFrom(other.getClass()) && ((IRegion) other).getID().equals(getID());
	}
}
