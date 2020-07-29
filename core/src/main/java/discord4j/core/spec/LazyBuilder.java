package discord4j.core.spec;

import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface LazyBuilder<T> extends Supplier<T> {

    default LazyBuilder<T> andThen(Function<T, T> f) {
        return () -> f.apply(this.get());
    }
}
