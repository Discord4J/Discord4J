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

import discord4j.store.util.MappingIterable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.function.Function;

/**
 * This provides a data connection which automatically maps values to keys in order to allow for simplified
 * operations.
 *
 * @param <K> The key type which provides a 1:1 mapping to the value type. This type is also expected to be
 *           {@link Comparable} in order to allow for range operations.
 * @param <V> The value type.
 *
 * @see StoreConnection
 * @see StoreConnection#withMapper(Function)
 */
public class MappedStoreConnection<K extends Comparable<K>, V> implements StoreConnection<K, V> {

    private final StoreConnection<K, V> connection;
    private final Function<V, K> idMapper;

    protected MappedStoreConnection(StoreConnection<K, V> connection, Function<V, K> idMapper) {
        this.connection = connection;
        this.idMapper = idMapper;
    }

    @Override
    public MappedStoreConnection<K, V> withMapper(Function<V, K> idMapper) {
        throw new UnsupportedOperationException("A mapper is already in use!");
    }

    @Override
    public Mono<Void> store(K key, V value) {
        return connection.store(key, value);
    }

    /**
     * Simplified version of {@link #store(Comparable, Object)} which doesn't require a key.
     *
     * @param value The value to store.
     * @return A mono to signal the completion of the storage of a value.
     *
     * @see #store(Comparable, Object)
     */
    public Mono<Void> storeValue(V value) {
        return connection.store(idMapper.apply(value), value);
    }

    @Override
    public Mono<Void> store(Mono<Tuple2<K, V>> entry) {
        return connection.store(entry);
    }

    /**
     * Simplified version of {@link #store(Mono)} which doesn't require a key.
     *
     * @param entry The mono providing a value to store.
     * @return A mono to signal the completion of the storage of a value.
     *
     * @see #store(Mono)
     */
    public Mono<Void> storeValue(Mono<V> entry) {
        return connection.store(entry.map(it -> Tuples.of(idMapper.apply(it), it)));
    }

    @Override
    public Mono<Void> store(Iterable<Tuple2<K, V>> entries) {
        return connection.store(entries);
    }

    /**
     * Simplified version of {@link #store(Iterable)} which doesn't require keys.
     *
     * @param entries The iterable providing values to store.
     * @return A mono to signal the completion of the storage of the values.
     *
     * @see #store(Iterable)
     */
    public Mono<Void> storeValues(Iterable<V> entries) {
        return connection.store(new MappingIterable<>(it -> Tuples.of(idMapper.apply(it), it), entries));
    }

    @Override
    public Mono<Void> store(Flux<Tuple2<K, V>> entryStream) {
        return connection.store(entryStream);
    }

    /**
     * Simplified version of {@link #store(Flux)} which doesn't require keys.
     *
     * @param entryStream The flux providing values to store.
     * @return A mono to signal the completion of the storage of the values.
     *
     * @see #store(Flux)
     */
    public Mono<Void> storeValues(Flux<V> entryStream) {
        return connection.store(entryStream.map(it -> Tuples.of(idMapper.apply(it), it)));
    }

    @Override
    public Mono<V> find(K id) {
        return connection.find(id);
    }

    @Override
    public Mono<V> find(Mono<K> id) {
        return connection.find(id);
    }

    @Override
    public Mono<Boolean> exists(K id) {
        return connection.exists(id);
    }

    @Override
    public Mono<Boolean> exists(Mono<K> id) {
        return connection.exists(id);
    }

    @Override
    public Mono<Boolean> exists(Flux<K> ids) {
        return connection.exists(ids);
    }

    @Override
    public Flux<V> findAll() {
        return connection.findAll();
    }

    @Override
    public Flux<V> findAll(Iterable<K> ids) {
        return connection.findAll(ids);
    }

    @Override
    public Flux<V> findAll(Flux<K> ids) {
        return connection.findAll(ids);
    }

    @Override
    public Flux<V> findInRange(K start, K end) {
        return connection.findInRange(start, end);
    }

    @Override
    public Mono<Long> count() {
        return connection.count();
    }

    @Override
    public Mono<Void> delete(K id) {
        return connection.delete(id);
    }

    @Override
    public Mono<Void> delete(Mono<K> id) {
        return connection.delete(id);
    }

    @Override
    public Mono<Void> delete(Flux<K> ids) {
        return connection.delete(ids);
    }

    @Override
    public Mono<Void> delete(Tuple2<K, V> entry) {
        return connection.delete(entry);
    }

    @Override
    public Mono<Void> deleteInRange(K start, K end) {
        return connection.deleteInRange(start, end);
    }

    /**
     * Simplified version of {@link #delete(Comparable)} which doesn't require a key.
     *
     * @param entry The value to delete.
     * @return A mono to signal the completion of the deletion of a value.
     *
     * @see #delete(Tuple2)
     */
    public Mono<Void> deleteValue(V entry) {
        return connection.delete(Tuples.of(idMapper.apply(entry), entry));
    }

    @Override
    public Mono<Void> deleteAll(Iterable<Tuple2<K, V>> entries) {
        return connection.deleteAll(entries);
    }

    /**
     * Simplified version of {@link #deleteAll(Iterable)} which doesn't require keys.
     *
     * @param entries The values to delete.
     * @return A mono to signal the completion of the deletion of values.
     *
     * @see #deleteAll(Iterable)
     */
    public Mono<Void> deleteAllValues(Iterable<V> entries) {
        return connection.deleteAll(new MappingIterable<>(it -> Tuples.of(idMapper.apply(it), it), entries));
    }

    @Override
    public Mono<Void> deleteAll(Flux<Tuple2<K, V>> entries) {
        return connection.deleteAll(entries);
    }

    /**
     * Simplified version of {@link #deleteAll(Flux)} which doesn't require keys.
     *
     * @param entries The values to delete.
     * @return A mono to signal the completion of the deletion of values.
     *
     * @see #deleteAll(Flux)
     */
    public Mono<Void> deleteAllValues(Flux<V> entries) {
        return connection.deleteAll(entries.map(it -> Tuples.of(idMapper.apply(it), it)));
    }

    @Override
    public Mono<Void> deleteAll() {
        return connection.deleteAll();
    }

    @Override
    public Flux<K> keys() {
        return connection.keys();
    }

    @Override
    public Flux<V> values() {
        return connection.values();
    }

    @Override
    public Flux<Tuple2<K, V>> entries() {
        return connection.entries();
    }

    @Override
    public void close() throws RuntimeException {
        connection.close();
    }
}
