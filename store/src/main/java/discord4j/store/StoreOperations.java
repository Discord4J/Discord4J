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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.store;

import discord4j.store.primitive.LongObjStoreOperations;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * This provides an active data connection to a store's data source.
 *
 * @param <K> The key type which provides a 1:1 mapping to the value type. This type is also expected to be
 *           {@link Comparable} in order to allow for range operations.
 * @param <V> The value type.
 *
 * @see LongObjStoreOperations
 */
public interface StoreOperations<K extends Comparable<K>, V> extends AutoCloseable {

    /**
     * Stores a key value pair.
     *
     * @param key The key representing the value.
     * @param value The value.
     * @return A mono which signals the completion of the storage of the pair.
     */
    Mono<Void> store(K key, V value);

    /**
     * Stores key value pairs.
     *
     * @param entries A mono providing the key value pairs.
     * @return A mono which signals the completion of the storage of the pairs.
     */
    Mono<Void> store(Iterable<Tuple2<K, V>> entries);

    /**
     * Stores key value pairs.
     *
     * @param entryStream A flux providing the key value pairs.
     * @return A mono which signals the completion of the storage of the pairs.
     */
    Mono<Void> store(Publisher<Tuple2<K, V>> entryStream);

    /**
     * Attempts to find the value associated with the provided id.
     *
     * @param id The id to search with.
     * @return A mono, which may or may not contain an associated object.
     */
    Mono<V> find(K id);

    /**
     * Checks if a value is associated with the provided id.
     *
     * @param id The id to search with.
     * @return A mono which provides true or false, depending on whether the id is associated with a value.
     */
    Mono<Boolean> exists(K id);

    /**
     * Checks if values are associated with all of the provided ids.
     *
     * @param ids A flux providing a stream of ids to search for.
     * @return A mono which provides true or false, depending on whether all the ids a represented in the data source.
     */
    Mono<Boolean> exists(Publisher<K> ids);

    /**
     * Retrieves all stored values from the data source.
     *
     * @return A stream of all data objects from the data source.
     */
    Flux<V> findAll();

    /**
     * Retrieves all stored values from the data source which have a provided id.
     *
     * @param ids A set of ids to find values for.
     * @return A stream of id associated data objects from the data source.
     */
    Flux<V> findAll(Iterable<K> ids);

    /**
     * Retrieves all stored values from the data source which have a provided id.
     *
     * @param ids A stream of ids to find values for.
     * @return A stream of id associated data objects from the data source.
     */
    Flux<V> findAll(Publisher<K> ids);

    /**
     * Retrieves all stored values with ids within a provided range.
     *
     * @param start The starting key (inclusive).
     * @param end The ending key (exclusive).
     * @return The stream of values with ids within the provided range.
     */
    Flux<V> findInRange(K start, K end);

    /**
     * Retrieves the amount of stored values in the data source currently.
     *
     * @return A mono which provides the amount of stored values.
     */
    Mono<Long> count();

    /**
     * Deletes a value associated with the provided id.
     *
     * @param id The id of the value to delete.
     * @return A mono which signals the completion of the deletion of the value.
     */
    Mono<Void> delete(K id);

    /**
     * Deletes the values associated with the provided ids.
     *
     * @param ids A stream of ids to delete values for.
     * @return A mono which signals the completion of the deletion of the values.
     */
    Mono<Void> delete(Publisher<K> ids);

    /**
     * Deletes a key value pair.
     *
     * @param entry The entry to delete.
     * @return A mono which signals the completion of the deletion of the value.
     */
    Mono<Void> delete(Tuple2<K, V> entry);

    /**
     * Deletes values within a range of ids.
     *
     * @param start The starting key (inclusive).
     * @param end The ending key (exclusive).
     * @return A mono which signals the completion of the deletion of values.
     */
    Mono<Void> deleteInRange(K start, K end);

    /**
     * Deletes all provided entries.
     *
     * @param entries The entries to delete.
     * @return A mono which signals the completion of the deletion of values.
     */
    Mono<Void> deleteAll(Iterable<Tuple2<K, V>> entries);

    /**
     * Deletes all provided entries.
     *
     * @param entries A stream of entries to delete.
     * @return A mono which signals the completion of the deletion of values.
     */
    Mono<Void> deleteAll(Publisher<Tuple2<K, V>> entries);

    /**
     * Deletes all entries in the data source.
     *
     * @return A mono which signals the completion of the deletion of all values.
     */
    Mono<Void> deleteAll();

    /**
     * Gets a stream of all keys in the data source.
     *
     * @return The stream of keys stored.
     */
    Flux<K> keys();

    /**
     * Gets a stream of all values in the data source.
     *
     * @return The stream of values stored.
     */
    Flux<V> values();

    /**
     * Gets a stream of all entries in the data source.
     *
     * @return The stream of all entries stored.
     */
    default Flux<Tuple2<K, V>> entries() {
        return keys().zipWith(values());
    }

    /**
     * This is used to close an active connection to the data source, allowing for resources to be deallocated.
     */
    @Override
    void close() throws RuntimeException;
}
