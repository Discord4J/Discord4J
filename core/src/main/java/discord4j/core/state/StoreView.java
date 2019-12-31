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

package discord4j.core.state;

import discord4j.store.api.Store;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class StoreView<K extends Comparable<K>, V> {

    private final Store<K, V> backing;

    public StoreView(Store<K, V> backing) {
        this.backing = backing;
    }

    /**
     * Attempts to find the value associated with the provided id.
     *
     * @param id The id to search with.
     * @return A mono, which may or may not contain an associated object.
     */
    public Mono<V> find(K id) {
        return backing.find(id);
    }

    /**
     * Retrieves all stored values with ids within a provided range.
     *
     * @param start The starting key (inclusive).
     * @param end The ending key (exclusive).
     * @return The stream of values with ids within the provided range.
     */
    public Flux<V> findInRange(K start, K end) {
        return backing.findInRange(start, end);
    }

    /**
     * Retrieves the amount of stored values in the data source currently.
     *
     * @return A mono which provides the amount of stored values.
     */
    public Mono<Long> count() {
        return backing.count();
    }

    /**
     * Gets a stream of all keys in the data source.
     *
     * @return The stream of keys stored.
     */
    public Flux<K> keys() {
        return backing.keys();
    }

    /**
     * Gets a stream of all values in the data source.
     *
     * @return The stream of values stored.
     */
    public Flux<V> values() {
        return backing.values();
    }

    /**
     * Gets a stream of all entries in the data source.
     *
     * @return The stream of all entries stored.
     */
    public Flux<Tuple2<K, V>> entries() {
        return backing.entries();
    }
}
