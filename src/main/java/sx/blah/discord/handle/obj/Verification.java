package sx.blah.discord.handle.obj;

/**
 * Represents a validation level for a guild
 */
public enum Verification {
	/**
	 * Represents a validation level of None
	 */
	NONE(0),
	/**
	 * Represents a validation level of Low
	 */
	LOW(1),
	/**
	 * Represents a validation level of Medium
	 */
	MEDIUM(2),
	/**
	 * Represents a validation level of (╯°□°）╯︵ ┻━┻
	 */
	HIGH(4);

	/**
	 * The validation level
	 */
	private int level;

	Verification(int level) { this.level = level; }

	/**
	 * Gets the Validation object for an integer level, or null if outside 0-3
	 *
	 * @param level The validation level as an int.
	 * @return The Validation object for the given level.
	 */
	public static Verification getByLevel(int level) {
		switch(level) {
			case 0: return NONE;
			case 1: return LOW;
			case 2: return MEDIUM;
			case 3: return HIGH;
			default: return null;
		}
	}

	/**
	 * Gets the level as an int
	 * @return The int corresponding to this validation level
	 */
	public int getLevel() {
		return level;
	}
}
