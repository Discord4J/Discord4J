package sx.blah.discord.handle.obj;

/**
 * Represents a verification level for a guild
 */
public enum Verification {
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
	HIGH;

	/**
	 * Gets the Verification object for an integer level, or null if outside 0-3
	 *
	 * @param level The verification level as an int.
	 * @return The Verification object for the given level.
	 */
	public static Verification getByLevel(int level) {
		if(level >= 0 && level <= 3)
			return Verification.values()[level];
		else return null;
	}

	/**
	 * Gets the level as an int
	 * @return The int corresponding to this verification level
	 */
	public int getLevel() {
		return this.ordinal();
	}
}
