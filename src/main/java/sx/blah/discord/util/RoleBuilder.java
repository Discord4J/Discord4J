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

package sx.blah.discord.util;

import sx.blah.discord.handle.impl.obj.Role;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;

import java.awt.Color;
import java.util.EnumSet;

/**
 * Used to configure and build a {@link IRole}.
 */
public class RoleBuilder {

	/**
	 * The parent guild of the role.
	 */
	private IGuild guild;
	/**
	 * The color of the role.
	 */
	private Color color;
	/**
	 * Whether the role is hoisted.
	 */
	private boolean hoist = false;
	/**
	 * Whether the role is mentionable.
	 */
	private boolean mentionable = false;
	/**
	 * The name of the role.
	 */
	private String name;
	/**
	 * The permissions granted to the role.
	 */
	private EnumSet<Permissions> permissions;

	public RoleBuilder(IGuild guild) {
		this.guild = guild;
	}

	/**
	 * Sets the color of the role.
	 *
	 * @param color The color of the role.
	 * @return The builder instance.
	 */
	public RoleBuilder withColor(Color color) {
		this.color = color;
		return this;
	}

	/**
	 * Sets whether to hoist the role.
	 *
	 * @param hoist Whether to hoist the role.
	 * @return The builder instance.
	 */
	public RoleBuilder setHoist(boolean hoist) {
		this.hoist = hoist;
		return this;
	}

	/**
	 * Sets whether the role is mentionable.
	 *
	 * @param mentionable Whether the role is mentionable.
	 * @return The builder instance.
	 */
	public RoleBuilder setMentionable(boolean mentionable) {
		this.mentionable = mentionable;
		return this;
	}

	/**
	 * Sets the name of the role.
	 *
	 * @param name The name of the role.
	 * @return The builder instance.
	 */
	public RoleBuilder withName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Sets the permissions granted to the role.
	 *
	 * @param permissions The permissions granted to the role.
	 * @return The builder instance.
	 */
	public RoleBuilder withPermissions(EnumSet<Permissions> permissions) {
		this.permissions = permissions;
		return this;
	}

	/**
	 * Builds a role with the configuration specified by the builder.
	 *
	 * @return A role with the configuration specified by the builder.
	 */
	public IRole build() {
		if (guild == null)
			throw new RuntimeException("A guild must be set to create a role.");

		Role role = (Role) guild.createRole();
		role.edit(color != null ? color : role.getColor(),
				hoist,
				name != null ? name : role.getName(),
				permissions != null ? permissions : role.getPermissions(),
				mentionable);
		return role;
	}
}
