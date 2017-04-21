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

package sx.blah.discord.util.cache;

import com.koloboke.collect.LongCursor;
import com.koloboke.collect.LongIterator;
import com.koloboke.collect.set.LongSet;
import com.koloboke.compile.ConcurrentModificationUnchecked;
import com.koloboke.compile.KolobokeMap;
import com.koloboke.compile.hash.algo.openaddressing.LinearProbing;
import com.koloboke.compile.mutability.Mutable;
import com.koloboke.function.LongObjConsumer;
import com.koloboke.function.LongObjPredicate;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

@KolobokeMap
@LinearProbing
@Mutable
@ConcurrentModificationUnchecked
public abstract class LongMap<T> {

	public static <T> LongMap<T> newMap() {
		return new KolobokeLongMap<>(8);
	}

	public static <T> LongMap<T> copyMap(LongMap<T> map) {
		LongMap<T> cache = newMap();
		map.keySet().forEach((long key) -> cache.put(key, map.get(key)));
		return cache;
	}

	@SuppressWarnings("unchecked")
	public static <T> LongMap<T> emptyMap() {
		return (LongMap<T>) EmptyLongMap.INSTANCE;
	}

	public abstract boolean containsKey(long key);

	public abstract T get(long key);

	public abstract T put(long key, T value);

	public abstract T remove(long key);

	public abstract void clear();

	public abstract int size();

	public abstract LongSet keySet();

	public abstract Collection<T> values();

	public abstract void forEach(LongObjConsumer<? super T> action);

	public abstract boolean forEachWhile(LongObjPredicate<? super T> predicate);

	public static final class EmptyLongMap<T> extends LongMap<T> {

		private static final LongMap<Object> INSTANCE = new EmptyLongMap<>();

		@Override
		public boolean containsKey(long key) {
			return false;
		}

		@Override
		public T get(long key) {
			return null;
		}

		@Override
		public T put(long key, T value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public T remove(long key) {
			return null;
		}

		@Override
		public void clear() {

		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public LongSet keySet() {
			return EmptyLongSet.INSTANCE;
		}

		@Override
		public Collection<T> values() {
			return Collections.emptyList();
		}

		@Override
		public void forEach(LongObjConsumer<? super T> action) {

		}

		@Override
		public boolean forEachWhile(LongObjPredicate<? super T> predicate) {
			return true;
		}
	}

	public static final class EmptyLongSet extends AbstractSet<Long> implements LongSet {

		public static final LongSet INSTANCE = new EmptyLongSet();

		@Override
		public boolean contains(long l) {
			return false;
		}

		@Override
		public long[] toLongArray() {
			return new long[0];
		}

		@Override
		public long[] toArray(long[] longs) {
			return new long[0];
		}

		@Override
		public LongCursor cursor() {
			return new LongCursor() {
				@Override
				public void forEachForward(LongConsumer longConsumer) {

				}

				@Override
				public long elem() {
					throw new UnsupportedOperationException();
				}

				@Override
				public boolean moveNext() {
					return false;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public void forEach(Consumer<? super Long> consumer) {

		}

		@Override
		public void forEach(LongConsumer longConsumer) {

		}

		@Override
		public boolean forEachWhile(LongPredicate longPredicate) {
			return false;
		}

		@Override
		public boolean add(long l) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeLong(long l) {
			return false;
		}

		@Override
		public boolean removeIf(Predicate<? super Long> predicate) {
			return false;
		}

		@Override
		public boolean removeIf(LongPredicate longPredicate) {
			return false;
		}

		@Override
		public long sizeAsLong() {
			return 0;
		}

		@Override
		public boolean ensureCapacity(long l) {
			return false;
		}

		@Override
		public boolean shrink() {
			return false;
		}

		@Override
		public LongIterator iterator() {
			return new LongIterator() {
				@Override
				public long nextLong() {
					throw new UnsupportedOperationException();
				}

				@Override
				public void forEachRemaining(Consumer<? super Long> consumer) {

				}

				@Override
				public void forEachRemaining(LongConsumer longConsumer) {

				}

				@Override
				public boolean hasNext() {
					return false;
				}
			};
		}

		@Override
		public int size() {
			return 0;
		}
	}
}
