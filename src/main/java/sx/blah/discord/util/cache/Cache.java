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

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.handle.obj.IIDLinkedObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * This represents an internal data structure for storing discord id, object pairs.
 *
 * @param <T> The object held by the cache
 */
public final class Cache<T  extends IIDLinkedObject> implements Map<String, T>, RandomAccess {

	/**
	 * This represents the default {@link ICacheDelegateProvider} used by Discord4J.
	 */
	public static final ICacheDelegateProvider DEFAULT_PROVIDER = new DefaultCacheDelegateProvider();
	/**
	 * This represents an implementation of {@link ICacheDelegateProvider} which stores nothing.
	 */
	public static final ICacheDelegateProvider IGNORING_PROVIDER = new IgnoringCacheDelegateProvider();

	private volatile ICacheDelegate<T> delegate;
	private final DiscordClientImpl client;

	public Cache(ICacheDelegate<T> delegate) {
		this(null, delegate);
	}

	public Cache(DiscordClientImpl client, ICacheDelegate<T> delegate) {
		this.delegate = delegate;
		this.client = client;
	}

	public Cache(DiscordClientImpl client, Class<T> self) {
		this(client, client.getCacheProvider().provide(self));
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
	 * Gets the client this cache is related to.
	 *
	 * @return The client instance.
	 */
	public IDiscordClient getClient() {
		return client;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return delegate.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return size() < 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsValue(Object value) {
		return value instanceof IIDLinkedObject && delegate.contains((T) value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	 * {@inheritDoc}
	 */
	@Override
	public T put(String key, T value) {
		if (!key.equals(value.getID()))
			throw new IllegalArgumentException("Key does not match the value's id");

		return put(value);
	}

	/**
	 * An implementation of {@link #put(String, T)} which accepts just an object (since it provides its respective id).
	 *
	 * @param value The key to search for.
	 * @return The previous object if it exists, or null if it doesn't.
	 *
	 * @see #put(String, T)
	 */
	public T put(T value) {
		return delegate.put(value).orElse(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	 * {@inheritDoc}
	 */
	@Override
	public void putAll(Map<? extends String, ? extends T> m) {
		delegate.putAll((Collection<T>) m.values());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		delegate.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> keySet() {
		return new HashSet<>(delegate.ids());
	}

	/**
	 * This gets a collection of ids which correspond to stored objects in this cache.
	 *
	 * @return A collection of ids of objects present.
	 *
	 * @see #keySet()
	 */
	public Collection<String> ids() {
		return delegate.ids();
	}

	/**
	 * This gets a collection of primitive ids which correspond to stored objects in this cache.
	 *
	 * @return A collection of ids of objects present.
	 *
	 * @see #keySet()
	 */
	public Collection<Long> longIDs() {
		return delegate.longIDs();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<T> values() {
		return delegate.values();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Entry<String, T>> entrySet() {
		Set<Entry<String, T>> set = new HashSet<>();
		delegate.ids().forEach(id -> set.add(new AbstractMap.SimpleEntry<String, T>(id, get(id))));
		return set;
	}

	/**
	 * An implementation of {@link #entrySet()} which returns an entry set with {@link Long} keys.
	 *
	 * @return The set of entries in this cache..
	 *
	 * @see #entrySet()
	 */
	public Set<Entry<Long, T>> longEntrySet() {
		Set<Entry<Long, T>> set = new HashSet<>();
		delegate.longIDs().forEach(id -> set.add(new AbstractMap.SimpleEntry<Long, T>(id, get(id))));
		return set;
	}

	/**
	 * This gets a copy of this cache.
	 *
	 * @return The new copy of the cache.
	 */
	public Cache<T> copy() {
		return new Cache<>(client, delegate.copy());
	}

	/**
	 * This represents an implementation of {@link ICacheDelegate} which is backed by a map.
	 */
	public static class MapCacheDelegate<T extends IIDLinkedObject> implements ICacheDelegate<T> {

		private final Map<String, T> backing;

		public MapCacheDelegate() {
			this(new ConcurrentHashMap<>());
		}

		public MapCacheDelegate(Map<String, T> backing) {
			this.backing = backing;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Optional<T> retrieve(String id) {
			return Optional.ofNullable(backing.get(id));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Optional<T> put(T obj) {
			return Optional.ofNullable(backing.put(obj.getID(), obj));
		}

		@Override
		public Optional<T> remove(String id) {
			return Optional.ofNullable(backing.remove(id));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Collection<T> clear() {
			Collection<T> cleared = values();
			backing.clear();
			return cleared;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean contains(String id) {
			return backing.containsKey(id);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int size() {
			return backing.size();
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
		public Collection<String> ids() {
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
			return new MapCacheDelegate<>(new ConcurrentHashMap<>(backing));
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
		public Optional<T> retrieve(String id) {
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
		public Optional<T> remove(String id) {
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
		public Collection<String> ids() {
			return Collections.emptySet();
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
