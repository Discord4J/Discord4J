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

package sx.blah.discord.api.internal.json.requests;

import sx.blah.discord.handle.obj.Permissions;

import java.awt.*;
import java.util.EnumSet;

/**
 * This is sent in order to edit a role.
 */
public class RoleEditRequest {

	/**
	 * The new color for the role.
	 */
	public int color;

	/**
	 * Whether to hoist the role.
	 */
	public boolean hoist;

	/**
	 * The new name of the role.
	 */
	public String name;

	/**
	 * The new permissions of the role.
	 */
	public int permissions;

	/**
	 * Whether the role is mentionable.
	 */
	public boolean mentionable;

	public RoleEditRequest(Color color, boolean hoist, String name, EnumSet<Permissions> permissions, boolean mentionable) {
		this.color = color.getRGB() & 0x00ffffff; // & 0x00ffffff eliminates the alpha value
		this.hoist = hoist;
		this.name = name;
		this.permissions = Permissions.generatePermissionsNumber(permissions);
		this.mentionable = mentionable;
	}
}
