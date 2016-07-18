package sx.blah.discord.api.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dynamically creates V objects using {@link #newObject()}
 * @param <K> The key that will be used to retrieve values.
 * @param <V> The value.
 *           TODO: Rename? I'm not sure the current name reflects functionality.
 */
public abstract class MappedPool<K, V> {
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
