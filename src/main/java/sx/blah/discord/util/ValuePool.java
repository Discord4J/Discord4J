package sx.blah.discord.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dynamically creates V objects using {@link #newObject()}
 * @param <K> The key that will be used to retrieve values.
 * @param <V> The value.
 */
public abstract class ValuePool<K, V> {
	private final Map<K, V> map = new ConcurrentHashMap<>();

	/**
	 * Generates a new object.
	 * @return The new object.
	 */
	public abstract V newObject();

	/**
	 * Gets the value associated with the key. If there is no associated value, generate one and return that.
	 * @param key The key
	 * @return The value associated with the key.
	 */
	public V get(K key) {
		if (!map.containsKey(key)) {
			map.put(key, newObject());
		}
		return map.get(key);
	}
}
