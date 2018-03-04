package sx.blah.discord.util;

import sx.blah.discord.handle.obj.IIDLinkedObject;
import sx.blah.discord.util.cache.LongMap;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A collector to build a {@link LongMap} from a stream of {@link IIDLinkedObject IIDLinkedObjects}.
 *
 * @param <T> The type of ID linked object.
 */
public class LongMapCollector<T extends IIDLinkedObject> implements Collector<T, List<T>, LongMap<T>> {

	private LongMapCollector() {

	}

	@Override
	public Supplier<List<T>> supplier() {
		return ArrayList::new;
	}

	@Override
	public BiConsumer<List<T>, T> accumulator() {
		return List::add;
	}

	@Override
	public BinaryOperator<List<T>> combiner() {
		return (left, right) -> {
			left.addAll(right);
			return left;
		};
	}

	@Override
	public Function<List<T>, LongMap<T>> finisher() {
		return ts -> {
			LongMap<T> map = LongMap.newMap();
			for (T t : ts) {
				map.put(t.getLongID(), t);
			}
			return map;
		};
	}

	@Override
	public Set<Characteristics> characteristics() {
		return EnumSet.of(Characteristics.UNORDERED);
	}

	public static <T extends IIDLinkedObject> Collector<T, ?, LongMap<T>> toLongMap() {
		return new LongMapCollector<>();
	}
}
