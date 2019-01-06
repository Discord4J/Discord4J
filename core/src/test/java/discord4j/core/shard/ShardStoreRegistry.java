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

package discord4j.core.shard;

import discord4j.store.api.Store;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ShardStoreRegistry {

    private final Map<Class<?>, Store<?, ?>> valueStore = new ConcurrentHashMap<>();
    private final Map<Integer, Map<Class<?>, Set<?>>> keyStores = new ConcurrentHashMap<>();

    boolean containsStore(Class<?> valueClass) {
        return valueStore.containsKey(valueClass);
    }

    <V extends Serializable, K extends Comparable<K>> void putStore(Class<V> valueClass, Store<K, V> store) {
        valueStore.put(valueClass, store);
    }

    @SuppressWarnings("unchecked")
    <K extends Comparable<K>, V extends Serializable> Store<K, V> getValueStore(Class<K> key, Class<V> value) {
        return (Store<K, V>) valueStore.get(value);
    }

    @SuppressWarnings("unchecked")
    <K extends Comparable<K>, V extends Serializable> Set<K> getKeyStore(Class<V> valueClass, int shardId) {
        return (Set<K>) keyStores.computeIfAbsent(shardId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(valueClass, k -> ConcurrentHashMap.newKeySet());
    }
}
