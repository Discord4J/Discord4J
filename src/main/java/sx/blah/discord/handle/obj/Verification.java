package sx.blah.discord.handle.obj;

/**
 * Represents a validation level for a guild
 */
public enum Verification {
	/**
	 * Represents a validation level of None
	 */
	NONE,
	/**
	 * Represents a validation level of Low
	 */
	LOW,
	/**
	 * Represents a validation level of Medium
	 */
	MEDIUM,
	/**
	 * Represents a validation level of (╯°□°）╯︵ ┻━┻
	 */
	HIGH;

	/**
	 * Gets the Validation object for an integer level, or null if outside 0-3
	 *
	 * @param level The validation level as an int.
	 * @return The Validation object for the given level.
	 */
	public static Verification getByLevel(int level) {
		try {
			return Verification.values()[level];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Gets the level as an int
	 * @return The int corresponding to this validation level
	 */
	public int getLevel() {
		return this.ordinal();
	}
}
