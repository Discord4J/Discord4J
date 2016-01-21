package sx.blah.discord.api;

import sx.blah.discord.handle.obj.Permissions;

import java.util.EnumSet;
import java.util.StringJoiner;

/**
 * This exception is thrown when a user is missing the required permissions to perform an action.
 */
public class MissingPermissionsException extends Exception {
	
	private EnumSet<Permissions> missing;
	
	public MissingPermissionsException(EnumSet<Permissions> permissionsMissing) {
		super(getMessage(permissionsMissing));
		missing = permissionsMissing;
	}
	
	public MissingPermissionsException(String message) {
		super(message);
	}
	
	private static String getMessage(EnumSet<Permissions> permissions) {
		StringJoiner joiner = new StringJoiner(", ");
		for (Permissions permission : permissions)
			joiner.add(permission.name());
		return "Missing permissions: "+joiner.toString()+"!";
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
