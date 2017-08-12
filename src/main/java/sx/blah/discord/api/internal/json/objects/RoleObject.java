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

package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a json role object.
 */
public class RoleObject {
	/**
	 * The ID of the role.
	 */
	public String id;
	/**
	 * The name of the role.
	 */
	public String name;
	/**
	 * The color of the role.
	 */
	public int color;
	/**
	 * Whether the role should be displayed separately in the online users list.
	 */
	public boolean hoist;
	/**
	 * The position of the role.
	 */
	public int position;
	/**
	 * The permissions granted by this role.
	 */
	public int permissions;
	/**
	 * Whether the role is managed.
	 */
	public boolean managed;
	/**
	 * Whether the role is mentionable.
	 */
	public boolean mentionable;
}
