/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.handle.audit.entry.change;

import sx.blah.discord.api.internal.json.objects.audit.AuditLogChangeObject;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A map of {@link ChangeKey ChangeKeys} to {@link AuditLogChange AuditLogChanges}.
 *
 * <p>This is a utility to ensure type-safe access of audit log changes. As long as only
 * {@link Collector} is used to construct the backing map, and only the pre-defined {@link ChangeKey ChangeKeys} are
 * used, type-safe access is guaranteed.
 */
@SuppressWarnings("unchecked")
public class ChangeMap {

	private final Map<ChangeKey<?>, AuditLogChange<?>> backing;

	public ChangeMap() {
		this(new HashMap<>());
	}

	private ChangeMap(Map<ChangeKey<?>, AuditLogChange<?>> backing) {
		this.backing = backing;
	}

	public <V> AuditLogChange<V> get(ChangeKey<V> key) {
		return (AuditLogChange<V>) backing.get(key);
	}

	/**
	 * A collector to build a {@link ChangeMap} from a stream of {@link AuditLogChangeObject AuditLogChangeObjects}.
	 */
	public static class Collector implements java.util.stream.Collector<AuditLogChangeObject, Map<ChangeKey<?>, AuditLogChange<?>>, ChangeMap> {

		private static final BinaryOperator<AuditLogChange<?>> mergeFunction = (u, v) -> {
			throw new IllegalStateException(String.format("Duplicate key %s", u));
		};

		private Collector() {

		}

		@Override
		public Supplier<Map<ChangeKey<?>, AuditLogChange<?>>> supplier() {
			return HashMap::new;
		}

		@Override
		public BiConsumer<Map<ChangeKey<?>, AuditLogChange<?>>, AuditLogChangeObject> accumulator() {
			return (map, change) -> map.merge(
					ChangeKey.fromRaw(change.key),
					new AuditLogChange<>(change.old_value, change.new_value),
					mergeFunction);
		}

		@Override
		public BinaryOperator<Map<ChangeKey<?>, AuditLogChange<?>>> combiner() {
			return (m1, m2) -> {
				for (Map.Entry<ChangeKey<?>, AuditLogChange<?>> e : m2.entrySet()) {
					m1.merge(e.getKey(), e.getValue(), mergeFunction);
				}
				return m1;
			};
		}

		@Override
		public Function<Map<ChangeKey<?>, AuditLogChange<?>>, ChangeMap> finisher() {
			return ChangeMap::new;
		}

		@Override
		public Set<Characteristics> characteristics() {
			return EnumSet.of(Characteristics.UNORDERED);
		}

		public static Collector toChangeMap() {
			return new Collector();
		}
	}
}
