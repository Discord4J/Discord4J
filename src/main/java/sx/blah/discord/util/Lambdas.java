package sx.blah.discord.util;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;

/**
 * Utility class that provides functions for generating functional interfaces
 */
public class Lambdas {

	/**
	 * Gets an accumulator function for reducing a stream of {@link List} objects by concatenation.
	 *
	 * @param <T> The type parameter for the {@link List}.
	 * @return The {@link BinaryOperator} used for reduction.
	 */
	public static <T> BinaryOperator<List<T>> listReduction() {
		return (a, b)->{
			a.addAll(b);
			return a;
		};
	}

	/**
	 * Gets an accumulator function for reducing a stream of {@link Set} objects by concatenation.
	 *
	 * @param <T> The type parameter for the {@link Set}.
	 * @return The {@link BinaryOperator} used for reduction.
	 */
	public static <T> BinaryOperator<Set<T>> setReduction() {
		return (a, b)->{
			a.addAll(b);
			return a;
		};
	}

	/**
	 * Gets an accumulator function for reducing a stream of {@link EnumSet} objects by concatenation.
	 *
	 * @param <T> The type parameter for the {@link EnumSet}.
	 * @return The {@link BinaryOperator} used for reduction.
	 */
	public static <T extends Enum<T>> BinaryOperator<EnumSet<T>> enumSetReduction() {
		return (a, b)->{
			a.addAll(b);
			return a;
		};
	}

}
