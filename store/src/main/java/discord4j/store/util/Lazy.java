package discord4j.store.util;

import java.util.function.Supplier;

/**
 * A wrapper which lazily-initializes an object using the given supplier.
 *
 * @param <T> The type of the object to be initialized.
 */
public class Lazy<T> {

    private T obj;
    private final Supplier<T> supplier;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * Gets the inner object. If {@link #obj} is null, {@link #supplier} is invoked.
     *
     * @return The inner object.
     */
    public T get() {
        if (obj == null) obj = supplier.get();
        return obj;
    }
}
