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

/**
 * A simple multi-valued map that wraps a {@link LinkedHashMap} with {@link LinkedList} to store multiple values.
 * This class is NOT thread-safe.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class Multimap<K, V> implements Map<K, List<V>> {

    private final Map<K, List<V>> map;

    public Multimap() {
        this.map = new LinkedHashMap<>();
    }

    public Multimap(int initialCapacity) {
        this.map = new LinkedHashMap<>(initialCapacity);
    }

    public void add(K key, V value) {
        map.computeIfAbsent(key, k -> new LinkedList<>()).add(value);
    }

    public void addAll(K key, List<? extends V> values) {
        map.computeIfAbsent(key, k -> new LinkedList<>()).addAll(values);
    }

    public void addAll(Multimap<K, V> values) {
        values.forEach(this::addAll);
    }

    public void set(K key, V value) {
        List<V> list = new LinkedList<>();
        list.add(value);
        map.put(key, list);
    }

    public void setAll(Map<K, V> values) {
        values.forEach(this::set);
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
