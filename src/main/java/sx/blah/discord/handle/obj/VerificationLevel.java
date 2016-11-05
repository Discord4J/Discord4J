package sx.blah.discord.handle.obj;

/**
 * Represents a verification level for a guild
 */
public enum VerificationLevel {
	/**
	 * Represents a verification level of None
	 */
	NONE,
	/**
	 * Represents a verification level of Low
	 */
	LOW,
	/**
	 * Represents a verification level of Medium
	 */
	MEDIUM,
	/**
	 * Represents a verification level of (╯°□°）╯︵ ┻━┻
	 */
	HIGH,
	/**
	 * Unknown verification level
	 */
	UNKNOWN;

	public static VerificationLevel get(int id) {
		if (id >= values().length) {
			return UNKNOWN;
		} else {
			return values()[id];
		}
	}
}
