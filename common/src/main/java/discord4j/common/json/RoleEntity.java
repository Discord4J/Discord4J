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
package discord4j.common.json;

public class RoleEntity {

	private String id;
	private String name;
	private int color;
	private boolean hoist;
	private int position;
	private int permissions;
	private boolean managed;
	private boolean mentionable;

	public RoleEntity(String id, String name, int color, boolean hoist, int position, int permissions, boolean managed,
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean isHoist() {
		return hoist;
	}

	public void setHoist(boolean hoist) {
		this.hoist = hoist;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getPermissions() {
		return permissions;
	}

	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}

	public boolean isManaged() {
		return managed;
	}

	public void setManaged(boolean managed) {
		this.managed = managed;
	}

	public boolean isMentionable() {
		return mentionable;
	}

	public void setMentionable(boolean mentionable) {
		this.mentionable = mentionable;
	}
}
