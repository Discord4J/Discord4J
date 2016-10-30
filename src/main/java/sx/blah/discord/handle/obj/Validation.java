package sx.blah.discord.handle.obj;

/**
 * Represents a validation level for a guild
 */
public enum Validation {
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

	private int level;

	Validation(int level) { this.level = level; }

	public int getLevel() {
		return level;
	}
}
