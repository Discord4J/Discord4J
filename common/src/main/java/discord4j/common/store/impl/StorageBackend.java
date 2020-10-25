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

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.UnaryOperator;

/**
 * Factory for a ConcurrentMap backing the different storages. You may use one of the static methods of this
 * interface to get instances.
 */
public interface StorageBackend {

    /**
     * Represents a {@link ConcurrentHashMap} backend.
     *
     * @return a {@link StorageBackend}
     */
    static StorageBackend concurrentHashMap() {
        return ConcurrentHashMap::new;
    }

    /**
     * Represents a {@link Caffeine} backend with the given configuration.
     *
     * @param caffeineBuilder a transformer for a {@link Caffeine} builder
     * @return a {@link StorageBackend}
     */
    static StorageBackend caffeine(UnaryOperator<Caffeine<Object, Object>> caffeineBuilder) {
        return new StorageBackend() {
            @Override
            public <K, V> ConcurrentMap<K, V> newMap() {
                return caffeineBuilder.apply(Caffeine.newBuilder()).<K, V>build().asMap();
            }
        };
    }

    <K, V> ConcurrentMap<K, V> newMap();
}
