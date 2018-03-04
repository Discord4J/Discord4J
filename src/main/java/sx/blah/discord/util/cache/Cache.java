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

import com.koloboke.collect.set.LongSet;
import com.koloboke.function.LongObjConsumer;
import com.koloboke.function.LongObjFunction;
import com.koloboke.function.LongObjPredicate;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.handle.obj.IIDLinkedObject;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An internal data structure for storing {@link IIDLinkedObject}s.
 *
 * @param <T> The type of object stored by the cache.
 */
public final class Cache<T extends IIDLinkedObject> implements Iterable<T> {

	/**
	 * The default cache delegate provider used by Discord4J.
	 */
	public static final ICacheDelegateProvider DEFAULT_PROVIDER = new DefaultCacheDelegateProvider();
	/**
	 * A cache delegate provider which stores nothing.
	 */
	public static final ICacheDelegateProvider IGNORING_PROVIDER = new IgnoringCacheDelegateProvider();

	/**
	 * The cache's underlying delegate.
	 */
	private volatile ICacheDelegate<T> delegate;

	public Cache(ICacheDelegate<T> delegate) {
		this.delegate = delegate;
	}

	public Cache(DiscordClientImpl client, Class<T> self) {
		this(client.getCacheProvider().provide(self));
	}

	/**
	 * Sets the cache's delegate.
	 *
	 * <p>NOTE: No data is copied from the old delegate to the new one.
	 *
	 * @param delegate The new delegate.
	 */
	public void setDelegate(ICacheDelegate<T> delegate) {
		this.delegate = delegate;
	}

	/**
	 * Gets the cache's delegate.
	 *
	 * @return The cache delegate.
	 */
	public ICacheDelegate<T> getDelegate() {
		return delegate;
	}

	/**
	 * Gets the number of elements stored in the cache.
	 *
	 * @return The number of elements stored in the cache.
	 */
	public int size() {
		return delegate.size();
	}

	/**
	 * Gets whether the cache is empty.
	 *
	 * @return Whether the cache is empty.
	 */
	public boolean isEmpty() {
		return size() < 1;
	}

	/**
	 * Gets whether the given key is present in the cache.
	 *
	 * @param key The key to search for.
	 * @return Whether the given key is present in the cache.
	 */
	public boolean containsKey(Object key) {
		if (key instanceof String) {
			return delegate.contains((String) key);
		} else if (key instanceof Long) {
			return delegate.contains((Long) key);
		} else {
			return false;
		}
	}

	/**
	 * Gets whether the given key is present in the cache.
	 *
	 * @param key The key to search for.
	 * @return Whether the given key is present in the cache.
	 */
	public boolean containsKey(long key) {
		return delegate.contains(key);
	}

	/**
	 * Gets whether the given value is present in the cache.
	 *
	 * @param value The value to search for.
	 * @return Whether the given value is present in the cache.
	 */
	public boolean containsValue(Object value) {
		return value instanceof IIDLinkedObject && delegate.contains((T) value);
	}

	/**
	 * Gets an object by its unique snowflake ID.
	 *
	 * @param key The ID of the desired object.
	 * @return The object with the provided ID (or null if one was not found).
	 */
	public T get(Object key) {
		if (key instanceof String) {
			return delegate.retrieve((String) key).orElse(null);
		} else if (key instanceof Long) {
			return delegate.retrieve((Long) key).orElse(null);
		} else {
			return null;
		}
	}

	/**
	 * Gets an object by its unique snowflake ID.
	 *
	 * @param key The ID of the desired object.
	 * @return The object with the provided ID (or null if one was not found).
	 */
	public T get(long key) {
		return delegate.retrieve(key).orElse(null);
	}

	/**
	 * Gets an object by its unique snowflake ID. If no object is found, the supplier must supply the object instead.
	 *
	 * @param key The ID of the desired object.
	 * @param supplier The supplier to invoke if no object is found.
	 * @return The object with provided ID (or the result of the supplier).
	 */
	public T getOrElseGet(Object key, Supplier<? extends T> supplier) {
		T val = get(key);
		return val == null ? supplier.get() : val;
	}

	/**
	 * Gets an object by its unique snowflake ID. If no object is found, the supplier must supply the object instead.
	 *
	 * @param key The ID of the desired object.
	 * @param supplier The supplier to invoke if no object is found.
	 * @return The object with provided ID (or the result of the supplier).
	 */
	public T getOrElseGet(long key, Supplier<? extends T> supplier) {
		T val = get(key);
		return val == null ? supplier.get() : val;
	}

	/**
	 * Puts an object into the cache.
	 *
	 * @param value The object to put.
	 * @return The previous object that had the same ID or null if there was not one.
	 */
	public T put(T value) {
		return delegate.put(value).orElse(null);
	}

	/**
	 * Puts an object into the cache if there is not already a value associated with the given key.
	 *
	 * @param id The ID to associate with the value.
	 * @param valueSupplier The supplier to invoke if the key is absent.
	 * @return Null.
	 */
	public T putIfAbsent(String id, Supplier<T> valueSupplier) {
		if (containsKey(id))
			return null;
		else
			return put(valueSupplier.get());
	}

	/**
	 * Puts an object into the cache if there is not already a value associated with the given key.
	 *
	 * @param id The ID to associate with the value.
	 * @param valueSupplier The supplier to invoke if the key is absent.
	 * @return Null.
	 */
	public T putIfAbsent(long id, Supplier<T> valueSupplier) {
		if (containsKey(id))
			return null;
		else
			return put(valueSupplier.get());
	}

	/**
	 * Removes an object from the cache.
	 *
	 * @param obj The ID of the object to remove or the object itself.
	 * @return The object that was removed.
	 */
	public T remove(Object obj) {
		if (obj instanceof String) {
			return delegate.remove((String) obj).orElse(null);
		} else if (obj instanceof Long) {
			return delegate.remove((Long) obj).orElse(null);
		} else if (obj instanceof IIDLinkedObject) {
			return delegate.remove((T) obj).orElse(null);
		} else {
			return null;
		}
	}

	/**
	 * Removes an object from the cache.
	 *
	 * @param key The ID of the object to remove.
	 * @return The object that was removed.
	 */
	public T remove(long key) {
		return delegate.remove(key).orElse(null);
	}

	/**
	 * Puts every element of the given cache into the cache.
	 *
	 * @param objs The objects to insert.
	 * @return Any objects that were replaced by the operation.
	 */
	public Collection<T> putAll(Cache<T> objs) {
		return putAll(objs.values());
	}

	/**
	 * Puts every element of the given collection into the cache.
	 *
	 * @param objs The objects to insert.
	 * @return Any objects that were replaced by the operation.
	 */
	public Collection<T> putAll(Collection<T> objs) {
		return delegate.putAll(objs);
	}

	/**
	 * Clears the cache.
	 */
	public void clear() {
		delegate.clear();
	}

	/**
	 * Gets the IDs of every object in the cache.
	 *
	 * @return The IDs of every object in the cache.
	 */
	public Collection<String> ids() {
		return delegate.ids();
	}

	/**
	 * Gets the IDs of every object in the cache.
	 *
	 * @return The IDs of every object in the cache.
	 */
	public Collection<Long> longIDs() {
		return delegate.longIDs();
	}

	/**
	 * Gets every value in the cache.
	 *
	 * @return Every value in the cache.
	 */
	public Collection<T> values() {
		return delegate.values();
	}

	/**
	 * See {@link Collection#stream()}.
	 */
	public Stream<T> stream() {
		return delegate.stream();
	}

	/**
	 * See {@link Collection#parallelStream()}.
	 */
	public Stream<T> parallelStream() {
		return delegate.parallelStream();
	}

	/**
	 * Gets a copy of the cache.
	 *
	 * @return A copy of the cache.
	 */
	public Cache<T> copy() {
		return new Cache<>(delegate.copy());
	}

	/**
	 * Gets a copy of the cache as a long map.
	 *
	 * @return A copy of the cache as a long map.
	 */
	public LongMap<T> mapCopy() {
		return delegate.mapCopy();
	}

	/**
	 * Performs the given action for each pair of key and value in the cache.
	 *
	 * @param action The action to perform for each pair of key and value in the cache.
	 */
	public void forEach(LongObjConsumer<? super T> action) {
		delegate.forEach(action);
	}

	/**
	 * Performs the given action for each pair of key and value in the cache while the function returns true.
	 *
	 * @param predicate The action to perform for each pair of key and value in the cache.
	 * @return Whether iterating was interrupted (whether the predicate ever returned false).
	 */
	public boolean forEachWhile(LongObjPredicate<? super T> predicate) {
		return delegate.forEachWhile(predicate);
	}

	/**
	 * Gets the first non-null value produced by the given function which is applied to every pair of keys and values
	 * in the cache.
	 *
	 * @param function The function to apply to each pair.
	 * @return The first non-null value produced by the given function
	 */
	public <Z> Z findResult(LongObjFunction<? super T, ? extends Z> function) {
		return delegate.findResult(function);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<T> iterator() {
		return delegate.iterator();
	}

	/**
	 * A cache delegate which is backed by a map.
	 */
	public static class MapCacheDelegate<T extends IIDLinkedObject> implements ICacheDelegate<T> {

		/**
		 * The backing map.
		 */
		private final LongMap<T> backing;
		/**
		 * The lock used for read and write operations.
		 */
		private final ReadWriteLock lock = new ReentrantReadWriteLock();

		public MapCacheDelegate() {
			this(LongMap.newMap());
		}

		public MapCacheDelegate(LongMap<T> backing) {
			this.backing = backing;
		}

		@Override
		public Optional<T> retrieve(long id) {
			lock.readLock().lock();
			try {
				return Optional.ofNullable(backing.get(id));
			} finally {
				lock.readLock().unlock();
			}
		}

		@Override
		public Optional<T> put(T obj) {
			lock.writeLock().lock();
			try {
				return Optional.ofNullable(backing.put(obj.getLongID(), obj));
			} finally {
				lock.writeLock().unlock();
			}
		}

		@Override
		public Optional<T> remove(long id) {
			lock.writeLock().lock();
			try {
				return Optional.ofNullable(backing.remove(id));
			} finally {
				lock.writeLock().unlock();
			}
		}

		@Override
		public Collection<T> clear() {
			lock.writeLock().lock();
			try {
				Collection<T> cleared = values();
				backing.clear();
				return cleared;
			} finally {
				lock.writeLock().unlock();
			}
		}

		@Override
		public boolean contains(long id) {
			lock.readLock().lock();
			try {
				return backing.containsKey(id);
			} finally {
				lock.readLock().unlock();
			}
		}

		@Override
		public int size() {
			lock.readLock().lock();
			try {
				return backing.size();
			} finally {
				lock.readLock().unlock();
			}
		}

		@Override
		public Iterator<T> iterator() {
			lock.readLock().lock();
			try {
				return backing.values().iterator();
			} finally {
				lock.readLock().unlock();
			}
		}

		@Override
		public LongSet longIDs() {
			lock.readLock().lock();
			try {
				return backing.keySet();
			} finally {
				lock.readLock().unlock();
			}
		}

		@Override
		public Collection<T> values() {
			lock.readLock().lock();
			try {
				return backing.values();
			} finally {
				lock.readLock().unlock();
			}
		}

		@Override
		public ICacheDelegate<T> copy() {
			return new MapCacheDelegate<>(mapCopy());
		}

		@Override
		public LongMap<T> mapCopy() {
			lock.readLock().lock();
			try {
				return LongMap.copyMap(backing);
			} finally {
				lock.readLock().unlock();
			}
		}

		@Override
		public void forEach(LongObjConsumer<? super T> action) {
			lock.readLock().lock();
			try {
				backing.forEach(action);
			} finally {
				lock.readLock().unlock();
			}
		}

		@Override
		public boolean forEachWhile(LongObjPredicate<? super T> predicate) {
			lock.readLock().lock();
			try {
				return backing.forEachWhile(predicate);
			} finally {
				lock.readLock().unlock();
			}
		}

		@Override
		public <Z> Z findResult(LongObjFunction<? super T, ? extends Z> function) {
			AtomicReference<Z> result = new AtomicReference<>();
			forEachWhile((key, value) -> {
				Z tmp = function.apply(key, value);
				if (tmp != null) {
					result.set(tmp);
					return false;
				}
				return true;
			});
			return result.get();
		}
	}

	/**
	 * A cache delegate which stores nothing.
	 */
	public static class IgnoringCacheDelegate<T extends IIDLinkedObject> implements ICacheDelegate<T> {

		/**
		 * Returns an empty optional.
		 *
		 * @return An empty optional.
		 */
		@Override
		public Optional<T> retrieve(long id) {
			return Optional.empty();
		}

		/**
		 * Returns an empty optional.
		 *
		 * @return An empty optional.
		 */
		@Override
		public Optional<T> put(T obj) {
			return Optional.empty();
		}

		/**
		 * Returns an empty optional.
		 *
		 * @return An empty optional.
		 */
		@Override
		public Optional<T> remove(long id) {
			return Optional.empty();
		}

		/**
		 * Returns an empty set.
		 *
		 * @return An empty set.
		 */
		@Override
		public Collection<T> clear() {
			return Collections.emptySet();
		}

		/**
		 * Returns 0.
		 *
		 * @return 0.
		 */
		@Override
		public int size() {
			return 0;
		}

		/**
		 * Returns an empty iterator.
		 *
		 * @return An empty iterator.
		 */
		@Override
		public Iterator<T> iterator() {
			return Collections.emptyIterator();
		}

		/**
		 * Returns an empty long set.
		 *
		 * @return An empty long set.
		 */
		@Override
		public LongSet longIDs() {
			return LongMap.EmptyLongSet.INSTANCE;
		}

		/**
		 * Returns an empty set.
		 *
		 * @return An empty set.
		 */
		@Override
		public Collection<T> values() {
			return Collections.emptySet();
		}

		/**
		 * Returns the same cache delegate instance.
		 *
		 * @return The same cache delegate instance.
		 */
		@Override
		public ICacheDelegate<T> copy() {
			return this;
		}

		/**
		 * Returns an empty long map.
		 *
		 * @return An empty long map.
		 */
		@Override
		public LongMap<T> mapCopy() {
			return LongMap.emptyMap();
		}

		/**
		 * No-op. Does nothing.
		 */
		@Override
		public void forEach(LongObjConsumer<? super T> action) {}

		/**
		 * No-op. Does nothing.
		 *
		 * @return True.
		 */
		@Override
		public boolean forEachWhile(LongObjPredicate<? super T> predicate) {
			return true;
		}

		/**
		 * No-op. Does nothing.
		 *
		 * @return Null.
		 */
		@Override
		public <Z> Z findResult(LongObjFunction<? super T, ? extends Z> function) {
			return null;
		}
	}
}

/**
 * The default cache delegate provider used by Discord4J. Always provides a {@link Cache.MapCacheDelegate}.
 */
class DefaultCacheDelegateProvider implements ICacheDelegateProvider {

	@Override
	public <T extends IIDLinkedObject> ICacheDelegate<T> provide(Class<T> clazz) {
		return new Cache.MapCacheDelegate<>();
	}
}

/**
 * A cache delegate provider which always provides {@link Cache.IgnoringCacheDelegate}.
 */
class IgnoringCacheDelegateProvider implements ICacheDelegateProvider {

	@Override
	public <T extends IIDLinkedObject> ICacheDelegate<T> provide(Class<T> clazz) {
		return new Cache.IgnoringCacheDelegate<>();
	}
}
