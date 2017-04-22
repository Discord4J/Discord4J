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

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This represents an internal data structure for storing discord id, object pairs.
 *
 * @param <T> The object held by the cache
 */
public final class Cache<T extends IIDLinkedObject> implements Iterable<T> {

	/**
	 * This represents the default {@link ICacheDelegateProvider} used by Discord4J.
	 */
	public static final ICacheDelegateProvider DEFAULT_PROVIDER = new DefaultCacheDelegateProvider();
	/**
	 * This represents an implementation of {@link ICacheDelegateProvider} which stores nothing.
	 */
	public static final ICacheDelegateProvider IGNORING_PROVIDER = new IgnoringCacheDelegateProvider();

	private volatile ICacheDelegate<T> delegate;

	public Cache(ICacheDelegate<T> delegate) {
		this.delegate = delegate;
	}

	public Cache(DiscordClientImpl client, Class<T> self) {
		this(client.getCacheProvider().provide(self));
	}

	/**
	 * Sets the {@link ICacheDelegate} used.
	 * NOTE: No data is copied from the old delegate to the new one.
	 *
	 * @param delegate The new delegate.
	 */
	public void setDelegate(ICacheDelegate<T> delegate) {
		this.delegate = delegate;
	}

	/**
	 * Gets the {@link ICacheDelegate} used by this cache.
	 *
	 * @return The current delegate.
	 */
	public ICacheDelegate<T> getDelegate() {
		return delegate;
	}

	/**
	 * Gets the amount of elements stored in this cache.
	 *
	 * @return The number of contained elements.
	 */
	public int size() {
		return delegate.size();
	}

	/**
	 * Checks if the cache is empty.
	 *
	 * @return True if the cache is empty, false if otherwise.
	 */
	public boolean isEmpty() {
		return size() < 1;
	}

	/**
	 * Checks whether a key is present in the cache.
	 *
	 * @return True if the key is present, false if otherwise.
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
	 * An implementation of {@link #containsKey(Object)} which accepts a primitive long key.
	 *
	 * @param key The key to search for.
	 * @return True if the key exists, false if otherwise.
	 *
	 * @see #containsKey(Object)
	 */
	public boolean containsKey(long key) {
		return delegate.contains(key);
	}

	/**
	 * Checks whether a value is existent in the cache.
	 *
	 * @return True if the value exists, false if otherwise.
	 */
	public boolean containsValue(Object value) {
		return value instanceof IIDLinkedObject && delegate.contains((T) value);
	}

	/**
	 * Gets an object by the specified id.
	 *
	 * @return The object, or null if not found.
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
	 * An implementation of {@link #get(Object)} which accepts a primitive long key.
	 *
	 * @param key The key to search for.
	 * @return The object if it exists, or null if it doesn't.
	 *
	 * @see #get(Object)
	 */
	public T get(long key) {
		return delegate.retrieve(key).orElse(null);
	}

	/**
	 * This attempts to get an object, if there is no object then the supplier is invoked.
	 *
	 * @param key The key to search for.
	 * @param supplier The supplier to use when the key is nonexistent.
	 * @return The object or null if not found.
	 */
	public T getOrElseGet(Object key, Supplier<? extends T> supplier) {
		T val = get(key);
		return val == null ? supplier.get() : val;
	}

	/**
	 * This attempts to get an object, if there is no object then the supplier is invoked.
	 *
	 * @param key The key to search for.
	 * @param supplier The supplier to use when the key is nonexistent.
	 * @return The object or null if not found.
	 */
	public T getOrElseGet(long key, Supplier<? extends T> supplier) {
		T val = get(key);
		return val == null ? supplier.get() : val;
	}

	/**
	 * An puts an object into the cache.
	 *
	 * @param value The key to search for.
	 * @return The previous object if it exists, or null if it doesn't.
	 */
	public T put(T value) {
		return delegate.put(value).orElse(null);
	}

	/**
	 * Puts a value into the cache if there is no value associated with it already.
	 *
	 * @param id The id associated with the object.
	 * @param valueSupplier The supplier to use if there is no object.
	 * @return The previous object if it exists, or null if it doesn't.
	 */
	public T putIfAbsent(String id, Supplier<T> valueSupplier) {
		if (containsKey(id))
			return null;
		else
			return put(valueSupplier.get());
	}

	/**
	 * Puts a value into the cache if there is no value associated with it already.
	 *
	 * @param id The id associated with the object.
	 * @param valueSupplier The supplier to use if there is no object.
	 * @return The previous object if it exists, or null if it doesn't.
	 */
	public T putIfAbsent(long id, Supplier<T> valueSupplier) {
		if (containsKey(id))
			return null;
		else
			return put(valueSupplier.get());
	}

	/**
	 * Attempts to remove an object from the cache.
	 *
	 * @param obj The object removed, this could either be a key or the object itself.
	 * @return The object removed.
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
	 * An implementation of {@link #remove(Object)} which accepts a primitive long key.
	 *
	 * @param key The key to search for.
	 * @return The previous object if it exists, or null if it doesn't.
	 *
	 * @see #remove(Object)
	 */
	public T remove(long key) {
		return delegate.remove(key).orElse(null);
	}

	/**
	 * This is called to place a cache of objects into the cache.
	 *
	 * @param objs The cache to insert.
	 * @return The objects replaced by this operation.
	 */
	public Collection<T> putAll(Cache<T> objs) {
		return putAll(objs.values());
	}

	/**
	 * This is called to place a collection of objects into the cache.
	 *
	 * @param objs The objects to insert.
	 * @return The objects replaced by this operation.
	 */
	public Collection<T> putAll(Collection<T> objs) {
		return delegate.putAll(objs);
	}

	/**
	 * This clears the cache.
	 */
	public void clear() {
		delegate.clear();
	}

	/**
	 * This gets a collection of ids which correspond to stored objects in this cache.
	 *
	 * @return A collection of ids of objects present.
	 */
	public Collection<String> ids() {
		return delegate.ids();
	}

	/**
	 * This gets a collection of primitive ids which correspond to stored objects in this cache.
	 *
	 * @return A collection of ids of objects present.
	 */
	public Collection<Long> longIDs() {
		return delegate.longIDs();
	}

	/**
	 * Gets a collection of the values stored by this cache.
	 *
	 * @return The values stored in this cache.
	 */
	public Collection<T> values() {
		return delegate.values();
	}

	/**
	 * Gets a stream of values from this cache.
	 *
	 * @return The stream of values.
	 */
	public Stream<T> stream() {
		return delegate.stream();
	}

	/**
	 * Gets a stream of values from this cache.
	 *
	 * @return The stream of values.
	 */
	public Stream<T> parallelStream() {
		return delegate.parallelStream();
	}

	/**
	 * This gets a copy of this cache.
	 *
	 * @return The new copy of the cache.
	 */
	public Cache<T> copy() {
		return new Cache<>(delegate.copy());
	}

	/**
	 * Creates a copy of this cache in a {@link Map}.
	 *
	 * @return The copy of the cache.
	 */
	public LongMap<T> mapCopy() {
		return delegate.mapCopy();
	}

	/**
	 * Optimized version of {@link #forEach(Consumer)}
	 *
	 * @param action Action to do with pairs of keys and values
	 */
	public void forEach(LongObjConsumer<? super T> action) {
		delegate.forEach(action);
	}

	/**
	 * Just like {@link #forEach(LongObjConsumer)}, but it stops when predicate returns false
	 *
	 * @param predicate Predicate, that consumes keys and values and produces false when iterating should be stopped
	 * @return true if iterating was interrupted
	 */
	public boolean forEachWhile(LongObjPredicate<? super T> predicate) {
		return delegate.forEachWhile(predicate);
	}

	/**
	 * Helper to do searching with transformation
	 *
	 * @param function Function, that accepts pair of key and value and produce some result
	 * @return First non-null result
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
	 * This represents an implementation of {@link ICacheDelegate} which is backed by a map.
	 */
	public static class MapCacheDelegate<T extends IIDLinkedObject> implements ICacheDelegate<T> {

		private final LongMap<T> backing;
		private final ReadWriteLock lock = new ReentrantReadWriteLock();

		public MapCacheDelegate() {
			this(LongMap.newMap());
		}

		public MapCacheDelegate(LongMap<T> backing) {
			this.backing = backing;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Optional<T> retrieve(long id) {
			lock.readLock().lock();
			try {
				return Optional.ofNullable(backing.get(id));
			} finally {
				lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
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

		/**
		 * {@inheritDoc}
		 */
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

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean contains(long id) {
			lock.readLock().lock();
			try {
				return backing.containsKey(id);
			} finally {
				lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int size() {
			lock.readLock().lock();
			try {
				return backing.size();
			} finally {
				lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Iterator<T> iterator() {
			return backing.values().iterator();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public LongSet longIDs() {
			return backing.keySet();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Collection<T> values() {
			return backing.values();
		}

		/**
		 * {@inheritDoc}
		 */
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
			backing.forEach(action);
		}

		@Override
		public boolean forEachWhile(LongObjPredicate<? super T> predicate) {
			return backing.forEachWhile(predicate);
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
	 * This represents an implementation of {@link ICacheDelegate} which ignores all objects.
	 */
	public static class IgnoringCacheDelegate<T extends IIDLinkedObject> implements ICacheDelegate<T> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Optional<T> retrieve(long id) {
			return Optional.empty();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Optional<T> put(T obj) {
			return Optional.empty();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Optional<T> remove(long id) {
			return Optional.empty();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Collection<T> clear() {
			return Collections.emptySet();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int size() {
			return 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Iterator<T> iterator() {
			return Collections.emptyIterator();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public LongSet longIDs() {
			return LongMap.EmptyLongSet.INSTANCE;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Collection<T> values() {
			return Collections.emptySet();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ICacheDelegate<T> copy() {
			return this;
		}

		@Override
		public LongMap<T> mapCopy() {
			return LongMap.emptyMap();
		}

		@Override
		public void forEach(LongObjConsumer<? super T> action) {

		}

		@Override
		public boolean forEachWhile(LongObjPredicate<? super T> predicate) {
			return true;
		}

		@Override
		public <Z> Z findResult(LongObjFunction<? super T, ? extends Z> function) {
			return null;
		}
	}
}

class DefaultCacheDelegateProvider implements ICacheDelegateProvider {

	@Override
	public <T extends IIDLinkedObject> ICacheDelegate<T> provide(Class<T> clazz) {
		return new Cache.MapCacheDelegate<>();
	}
}

class IgnoringCacheDelegateProvider implements ICacheDelegateProvider {

	@Override
	public <T extends IIDLinkedObject> ICacheDelegate<T> provide(Class<T> clazz) {
		return new Cache.IgnoringCacheDelegate<>();
	}
}
