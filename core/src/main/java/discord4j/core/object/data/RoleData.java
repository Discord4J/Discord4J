/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.data;

public class RoleData {

	private final long id;
	private final String name;
	private final int color;
	private final boolean hoist;
	private final int position;
	private final int permissions;
	private final boolean managed;
	private final boolean mentionable;

	public RoleData(long id, String name, int color, boolean hoist, int position, int permissions, boolean managed,
			boolean mentionable) {
		this.id = id;
		this.name = name;
		this.color = color;
		this.hoist = hoist;
		this.position = position;
		this.permissions = permissions;
		this.managed = managed;
		this.mentionable = mentionable;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getColor() {
		return color;
	}

	public boolean isHoist() {
		return hoist;
	}

	public int getPosition() {
		return position;
	}

	public int getPermissions() {
		return permissions;
	}

	public boolean isManaged() {
		return managed;
	}

	public boolean isMentionable() {
		return mentionable;
	}
}
