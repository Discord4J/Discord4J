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
package discord4j.core.object.entity;

import discord4j.core.object.PermissionSet;

/** Explicit permission overwrite for various {@link Type types}. */
public interface PermissionOverwrite extends Entity {

	/**
	 * Gets the type of entity this overwrite is for.
	 *
	 * @return The type of entity this overwrite is for.
	 */
	Type getType();

	/**
	 * Gets the permissions explicitly allowed for this overwrite.
	 *
	 * @return The permissions explicitly allowed for this overwrite.
	 */
	PermissionSet getAllowed();

	/**
	 * Gets the permissions explicitly denied for this overwrite.
	 *
	 * @return The permissions explicitly denied for this overwrite.
	 */
	PermissionSet getDenied();

	/** The type of entity a {@link PermissionOverwrite} is explicitly for. */
	enum Type {

		/** The {@link Role} entity. */
		ROLE("role"),

		/** The {@link Member} entity. */
		MEMBER("member");

		/** The underlying value as represented by Discord. */
		private final String value;

		/**
		 * Constructs a {@code PermissionOverwrite.Type}.
		 *
		 * @param value The underlying value as represented by Discord.
		 */
		Type(final String value) {
			this.value = value;
		}

		/**
		 * Gets the underlying value as represented by Discord.
		 *
		 * @return The underlying value as represented by Discord.
		 */
		public String getValue() {
			return value;
		}
	}
}
