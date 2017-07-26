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

import sx.blah.discord.handle.obj.Permissions;

import java.util.EnumSet;
import java.util.StringJoiner;

/**
 * Thrown when an action is attempted that the user does not have the required permissions for.
 */
public class MissingPermissionsException extends RuntimeException {

	/**
	 * The permissions that are missing to perform the action.
	 */
	private final EnumSet<Permissions> missing;

	public MissingPermissionsException(String reason, EnumSet<Permissions> missing) {
		super(reason);
		this.missing = missing;
	}

	public MissingPermissionsException(EnumSet<Permissions> permissionsMissing) {
		super(getMessage(permissionsMissing));
		missing = permissionsMissing;
	}

	/**
	 * Gets the error message with the missing permissions as a string.
	 *
	 * @param permissions The missing permissions.
	 * @return The error message with the missing permissions as a string.
	 */
	private static String getMessage(EnumSet<Permissions> permissions) {
		StringJoiner joiner = new StringJoiner(", ");
		permissions.stream()
				.map(Enum::name)
				.forEach(joiner::add);
		return "Missing permissions: " + joiner.toString() + "!";
	}

	/**
	 * Gets the missing permissions.
	 *
	 * @return The missing permissions.
	 */
	public EnumSet<Permissions> getMissingPermissions() {
		return missing;
	}

	/**
	 * Gets the formatted error message.
	 *
	 * @return The formatted error message.
	 */
	public String getErrorMessage() {
		if (missing == null)
			return getLocalizedMessage();
		return getMessage(missing);
	}
}
