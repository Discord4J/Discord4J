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
import com.github.benmanes.caffeine.cache.RemovalListener;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.UnaryOperator;

/**
 * Factory for a ConcurrentMap backing the different storages. You may use one of the static methods of this
 * interface to get instances.
 */
public interface StorageBackend {

    /**
     * Represents a backend that doesn't store anything.
     *
     * @return a {@link StorageBackend}
     */
    static StorageBackend noOp() {
        return new StorageBackend() {
            @Override
            public <K, V> ConcurrentMap<K, V> newMap(@Nullable RemovalListener<K, V> removalListener) {
                return new NoOpMap<>();
            }
        };
    }

    /**
     * Represents a {@link ConcurrentHashMap} backend.
     *
     * @return a {@link StorageBackend}
     */
    static StorageBackend concurrentHashMap() {
        return new StorageBackend() {
            @Override
            public <K, V> ConcurrentMap<K, V> newMap(@Nullable RemovalListener<K, V> removalListener) {
                return new ConcurrentHashMap<>();
            }
        };
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
            public <K, V> ConcurrentMap<K, V> newMap(@Nullable RemovalListener<K, V> removalListener) {
                Caffeine<Object, Object> configured = caffeineBuilder.apply(Caffeine.newBuilder());
                if (removalListener != null) {
                    return configured.removalListener(removalListener).build().asMap();
                }
                return configured.<K, V>build().asMap();
            }
        };
    }

    default <K, V> ConcurrentMap<K, V> newMap() {
        return newMap(null);
    }

    <K, V> ConcurrentMap<K, V> newMap(@Nullable RemovalListener<K, V> removalListener);
}
