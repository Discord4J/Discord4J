package sx.blah.discord.util;

import sx.blah.discord.handle.obj.Permissions;

import java.util.EnumSet;
import java.util.StringJoiner;

/**
 * This exception is thrown when a user is missing the required permissions to perform an action.
 */
public class MissingPermissionsException extends Exception {

	private final EnumSet<Permissions> missing;

	public MissingPermissionsException(String reason, EnumSet<Permissions> missing) {
		super(reason);
		this.missing = missing;
	}

	public MissingPermissionsException(EnumSet<Permissions> permissionsMissing) {
		super(getMessage(permissionsMissing));
		missing = permissionsMissing;
	}

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
	 * @return The permissions.
	 */
	public EnumSet<Permissions> getMissingPermissions() {
		return missing;
	}

	/**
	 * Gets the formatted error message.
	 *
	 * @return The message.
	 */
	public String getErrorMessage() {
		if (missing == null)
			return getLocalizedMessage();
		return getMessage(missing);
	}
}
