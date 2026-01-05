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

package discord4j.common.store.impl;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

class NoOpMap<K, V> implements ConcurrentMap<K, V> {
    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public @Nullable V get(Object key) {
        return null;
    }

    @Override
    public @Nullable V put(K key, V value) {
        return null;
    }

    @Override
    public @Nullable V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {}

    @Override
    public void clear() {}

    @Override
    public Set<K> keySet() {
        return Collections.emptySet();
    }

    @Override
    public Collection<V> values() {
        return Collections.emptyList();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.emptySet();
    }

    @Override
    public @Nullable V putIfAbsent(K key, V value) {
        return null;
    }

    @Override
    public boolean remove(Object key, Object value) {
        return false;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return oldValue == null;
    }

    @Override
    public @Nullable V replace(K key, V value) {
        return null;
    }
}
