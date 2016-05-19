package sx.blah.discord.api.internal;

import java.util.function.Supplier;

/**
 * This is used to ensure a value is accurately and lazily updated.
 *
 * @param <T> The value type this represents.
 */
public class TimedValue <T> {

	private volatile T value;
	private volatile long time;
	private final long timeToInvalidate;
	private final Supplier<T> supplier;

	public TimedValue(long timeToInvalidate, Supplier<T> valueSupplier) {
		this.timeToInvalidate = timeToInvalidate;
		this.supplier = valueSupplier;
	}

	/**
	 * Gets the value or if invalidated, uses the supplier.
	 *
	 * @return The value.
	 */
	public T get() {
		if (value == null || System.currentTimeMillis() - time >= timeToInvalidate) { //Lazy initialization ftw!
			value = supplier.get();
			time = System.currentTimeMillis();
		}

		return value;
	}
}
