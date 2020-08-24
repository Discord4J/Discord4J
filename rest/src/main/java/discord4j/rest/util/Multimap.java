/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.rest.util;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * A simple multi-valued map that wraps a {@link LinkedHashMap} with {@link LinkedList} to store multiple values.
 * This class is NOT thread-safe.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class Multimap<K, V> implements Map<K, List<V>> {

    private final Map<K, List<V>> map;

    /**
     * Create an empty Multimap.
     */
    public Multimap() {
        this.map = new LinkedHashMap<>();
    }

    /**
     * Create an empty Multimap with a given initial capacity.
     *
     * @param initialCapacity the initial capacity
     */
    public Multimap(int initialCapacity) {
        this.map = new LinkedHashMap<>(initialCapacity);
    }

    /**
     * Add a value to the list of values under the given key.
     *
     * @param key the key
     * @param value the value to add
     */
    public void add(K key, V value) {
        map.computeIfAbsent(key, k -> new LinkedList<>()).add(value);
    }

    /**
     * Add multiple values to the list of values under the given key.
     *
     * @param key the key
     * @param values the values to add
     */
    public void addAll(K key, Collection<? extends V> values) {
        map.computeIfAbsent(key, k -> new LinkedList<>()).addAll(values);
    }

    /**
     * Add all values from the given {@link Multimap} to the current ones.
     *
     * @param values the values to add
     */
    public void addAll(Multimap<K, V> values) {
        values.forEach(this::addAll);
    }

    /**
     * Set a value under the given key, replacing any existing single or multiple values.
     *
     * @param key the key
     * @param value the value to set
     */
    public void set(K key, V value) {
        List<V> list = new LinkedList<>();
        list.add(value);
        map.put(key, list);
    }

    /**
     * Set multiple values under the given key, replacing any existing single or multiple values.
     *
     * @param values the values to set
     */
    public void setAll(Map<K, V> values) {
        values.forEach(this::set);
    }

    /**
     * Clone this {@link Multimap} using a deep copy, including each stored value list.
     *
     * @return a deep copy of this {@code Multimap}
     */
    public Multimap<K, V> deepCopy() {
        Multimap<K, V> copy = new Multimap<>(map.size());
        map.forEach((key, value) -> copy.put(key, new LinkedList<>(value)));
        return copy;
    }

    /**
     * Performs the given action for each element, meaning once per each K-V entry in this Multimap, until all
     * entries have been processed or the action throws an exception.
     *
     * @param action The action to be performed for each entry
     * @throws NullPointerException if the specified action is null
     * @throws ConcurrentModificationException if an entry is found to be
     * removed during iteration
     * @since 3.1.1
     */
    public void forEachElement(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        for (Map.Entry<K, List<V>> entry : entrySet()) {
            K k;
            try {
                for (V v : entry.getValue()) {
                    k = entry.getKey();
                    action.accept(k, v);
                }
            } catch (IllegalStateException ise) {
                throw new ConcurrentModificationException(ise);
            }
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public List<V> get(Object key) {
        return map.get(key);
    }

    @Override
    public List<V> put(K key, List<V> value) {
        return map.put(key, value);
    }

    @Override
    public List<V> remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends List<V>> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<List<V>> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, List<V>>> entrySet() {
        return map.entrySet();
    }
}
