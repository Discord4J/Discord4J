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

/**
 * Registry that holds all {@link Store} and {@link KeyStore} instances in order to support a
 * {@link ShardAwareStoreService} creating and invalidating on a per-shard basis.
 */
public interface KeyStoreRegistry {

    /**
     * Return if this registry contains a {@link Store} instance for the given value class.
     *
     * @param valueClass the class to perform a lookup with
     * @return {@code true} if a {@link Store} for the given class exists, {@code false} otherwise.
     */
    boolean containsStore(Class<?> valueClass);

    /**
     * Add a {@link Store} to the registry under its value type.
     *
     * @param valueClass the key to used when adding {@code store}
     * @param store the {@link Store} instance to add to this registry
     * @param <V> the {@link Store} value type
     * @param <K> the {@link Store} key type
     */
    <V, K extends Comparable<K>> void putStore(Class<V> valueClass, Store<K, V> store);

    /**
     * Return the saved {@link Store} with the given key and value type as parameters.
     *
     * @param keyClass the target {@link Store} key class
     * @param valueClass the target {@link Store} value class
     * @param <K> the type of the given {@code key}
     * @param <V> the type of the given {@code value}
     * @return a {@link Store} of the given types
     */
    <K extends Comparable<K>, V> Store<K, V> getValueStore(Class<K> keyClass, Class<V> valueClass);

    /**
     * Return the saved {@link KeyStore} for the given value type.
     *
     * @param valueClass the target {@link Store} value class
     * @param <K> the type of the given {@code key}
     * @param <V> the type of the given {@code value}
     * @return a {@link KeyStore} for the given value type
     */
    <K extends Comparable<K>, V> KeyStore<K> getKeyStore(Class<V> valueClass);
}
