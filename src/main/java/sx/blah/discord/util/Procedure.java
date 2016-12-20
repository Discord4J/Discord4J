package sx.blah.discord.util;

/**
 * Represents an operation that accepts no input arguments and returns no result.
 * It is expected to operate via side-effects.
 */
@FunctionalInterface
public interface Procedure {
	void invoke();
}
