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

import java.awt.Color;
import java.util.EnumSet;

/**
 * Sent to edit a role's properties.
 */
public class RoleEditRequest {

	public static class Builder {

		private Color color;
		private Boolean hoist;
		private String name;
		private EnumSet<Permissions> permissions;
		private Boolean mentionable;

		/**
		 * Sets the new color for the role.
		 *
		 * @param color The new color.
		 * @return This builder, for chaining.
		 */
		public Builder color(Color color) {
			this.color = color;
			return this;
		}

		/**
		 * Sets whether to hoist the role.
		 *
		 * @param hoist If the role should be hoisted.
		 * @return This builder, for chaining.
		 */
		public Builder hoist(boolean hoist) {
			this.hoist = hoist;
			return this;
		}

		/**
		 * Sets a new name for this role.
		 *
		 * @param name The new name for the role.
		 * @return This builder, for chaining.
		 */
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Sets the new permissions for this role.
		 *
		 * @param permissions An EnumSet of Permissions for this role.
		 * @return This builder, for chaining.
		 */
		public Builder permissions(EnumSet<Permissions> permissions) {
			this.permissions = permissions;
			return this;
		}

		/**
		 * Sets whether the role is mentionable.
		 *
		 * @param mentionable If the role is mentionable.
		 * @return This builder, for chaining.
		 */
		public Builder mentionable(boolean mentionable) {
			this.mentionable = mentionable;
			return this;
		}

		/**
		 * Builds the request object.
		 *
		 * @return The role edit request.
		 */
		public RoleEditRequest build() {
			return new RoleEditRequest(color, hoist, name, permissions, mentionable);
		}
	}

	/**
	 * The new color of the role.
	 */
	private final Integer color;
	/**
	 * Whether the role is hoisted.
	 */
	private final Boolean hoist;
	/**
	 * The new name of the role.
	 */
	private final String name;
	/**
	 * The new permissions of the role.
	 */
	private final Integer permissions;
	/**
	 * Whether the role is mentionable.
	 */
	private final Boolean mentionable;

	RoleEditRequest(Color color, Boolean hoist, String name, EnumSet<Permissions> permissions, Boolean mentionable) {
		this.color = color == null ? null : color.getRGB() & 0x00ffffff; // & 0x00ffffff eliminates the alpha value
		this.hoist = hoist;
		this.name = name;
		this.permissions = permissions == null ? null : Permissions.generatePermissionsNumber(permissions);
		this.mentionable = mentionable;
	}

	public Integer getColor() {
		return color;
	}

	public Boolean getHoist() {
		return hoist;
	}

	public String getName() {
		return name;
	}

	public Integer getPermissions() {
		return permissions;
	}

	public Boolean getMentionable() {
		return mentionable;
	}
}
