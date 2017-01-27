package sx.blah.discord.util;

import java.util.function.Supplier;

/**
 * This is used to ensure a value is accurately and lazily updated.
 *
 * @param <T> The value type this represents.
 */
public class TimedValue<T> extends Lazy<T> {
	private final long timeToInvalidate;
	private long time;

	public TimedValue(long timeToInvalidate, Supplier<T> supplier) {
		super(supplier);
		this.timeToInvalidate = timeToInvalidate;
	}

	@Override
	public T get() {
		if (obj == null || System.currentTimeMillis() - time >= timeToInvalidate) {
			obj = supplier.get();
			time = System.currentTimeMillis();
		}
		return obj;
	}
}
