package sx.blah.discord.util;

import java.util.function.Supplier;

/**
 * Simple implementation of lazy initialization.
 *
 * @param <T> The type of the object to be initialized.
 */
public class Lazy<T> {
	protected T obj;
	protected final Supplier<T> supplier;

	public Lazy(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	public T get() {
		if (obj == null) obj = supplier.get();
		return obj;
	}
}
