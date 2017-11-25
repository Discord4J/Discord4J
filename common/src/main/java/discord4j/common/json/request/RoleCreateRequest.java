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
package discord4j.common.json.request;

public class RoleCreateRequest {

	private final String name;
	private final int permissions;
	private final int color;
	private final boolean hoist;
	private final boolean mentionable;

	public RoleCreateRequest(String name, int permissions, int color, boolean hoist, boolean mentionable) {
		this.name = name;
		this.permissions = permissions;
		this.color = color;
		this.hoist = hoist;
		this.mentionable = mentionable;
	}

	@Override
	public String toString() {
		return "RoleCreateRequest[" +
				"name='" + name + '\'' +
				", permissions=" + permissions +
				", color=" + color +
				", hoist=" + hoist +
				", mentionable=" + mentionable +
				']';
	}
}
