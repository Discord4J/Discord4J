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

import discord4j.store.dsl.LogicalStatement;
import discord4j.store.dsl.LogicalStatementFactory;
import discord4j.store.dsl.QueryBuilder;
import discord4j.store.dsl.QueryBuilderFactory;
import discord4j.store.primitive.LongObjStore;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.Serializable;

/**
 * This provides an active data connection to a store's data source.
 *
 * @param <K> The key type which provides a 1:1 mapping to the value type. This type is also expected to be
 * {@link Comparable} in order to allow for range operations.
 * @param <V> The value type, these follow
 * <a href="https://en.wikipedia.org/wiki/JavaBeans#JavaBean_conventions">JavaBean</a> conventions.
 * @see LongObjStore
 * @see discord4j.store.util.AbsentValue
 */
public interface Store<K extends Comparable<K>, V extends Serializable> {

    /**
     * Gets the class associated with the key type.
     *
     * @return The key type.
     */
    Class<K> getKeyType();

    /**
     * Gets the class associated with the value type.
     *
     * @return The value type.
     */
    Class<V> getValueType();

    QueryBuilderFactory<K, V, ? super LogicalStatementFactory<V>, ? super LogicalStatement<V>> queryBuilderFactory();

    /**
     * Starts a complex select query.
     *
     * @return The builder used to prepare the query.
     */
    default QueryBuilder<K, V, ? super LogicalStatementFactory<V>, ? super LogicalStatement<V>> query() {
        return queryBuilderFactory().create(this);
    }

    /**
     * Stores a key value pair.
     *
     * @param key The key representing the value.
     * @param value The value.
     * @return A mono which signals the completion of the storage of the pair.
     */
    Mono<Void> save(K key, V value);

    /**
     * Stores key value pairs.
     *
     * @param entryStream A flux providing the key value pairs.
     * @return A mono which signals the completion of the storage of the pairs.
     */
    Mono<Void> save(Publisher<Tuple2<K, V>> entryStream);

    /**
     * Attempts to find the value associated with the provided id.
     *
     * @param id The id to search with.
     * @return A mono, which may or may not contain an associated object.
     */
    Mono<V> find(K id);

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
     * Deletes values within a range of ids.
     *
     * @param start The starting key (inclusive).
     * @param end The ending key (exclusive).
     * @return A mono which signals the completion of the deletion of values.
     */
    Mono<Void> deleteInRange(K start, K end);

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
     * Invalidates the contents of the store. Once this is invoked, there is no longer a guarantee that the
     * data in the store is reliable.
     *
     * @return A mono which signals the completion of the invalidation of all values.
     */
    Mono<Void> invalidate();
}
