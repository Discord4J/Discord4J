package sx.blah.discord.handle.obj;

/**
 * The online statuses a user can be, such as ONLINE or IDLE.
 */
public enum StatusType {

	/**
	 * Represents that the user is online.
	 */
	ONLINE,
	/**
	 * Represents that the user is idle.
	 */
	IDLE,
	/**
	 * Represents that the user is offline.
	 */
	OFFLINE,
	/**
	 * Represents that the user is streaming.
	 */
	STREAMING,
	/**
	 * Represents that the user is in 'do not disturb' mode.
	 */
	DND,
	/**
	 * Unknown presence.
	 */
	UNKNOWN;

	public static StatusType get(String name) {
		try {
			return valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return UNKNOWN;
		}
	}

}
