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
package discord4j.store.primitive;

import discord4j.store.MappedStoreConnection;
import discord4j.store.util.LongObjTuple2;
import discord4j.store.util.MappingIterable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.ToLongFunction;

/**
 * This provides a data connection which automatically maps values to keys in order to allow for simplified
 * operations.
 *
 * @param <V> The value type.
 *
 * @see LongObjStoreConnection
 * @see LongObjStoreConnection#withMapper(ToLongFunction)
 */
public class LongObjMappedStoreConnection<V> extends MappedStoreConnection<Long, V> implements LongObjStoreConnection<V> {

    private final LongObjStoreConnection<V> connection;
    private final ToLongFunction<V> idMapper;

    protected LongObjMappedStoreConnection(LongObjStoreConnection<V> connection, ToLongFunction<V> idMapper) {
        super(connection, idMapper::applyAsLong);
        this.connection = connection;
        this.idMapper = idMapper;
    }

    @Override
    public LongObjMappedStoreConnection<V> withMapper(Function<V, Long> idMapper) {
        throw new UnsupportedOperationException("A mapper is already in use!");
    }

    @Override
    public LongObjMappedStoreConnection<V> withMapper(ToLongFunction<V> idMapper) {
        throw new UnsupportedOperationException("A mapper is already in use!");
    }

    @Override
    public Mono<Void> storeWithLong(long key, V value) {
        return connection.storeWithLong(key, value);
    }

    /**
     * Simplified version of {@link #storeWithLong(long, Object)} which doesn't require a key.
     *
     * @param value The value to store.
     * @return A mono to signal the completion of the storage of a value.
     *
     * @see #storeWithLong(long, Object)
     */
    public Mono<Void> storeValue(V value) {
        return connection.storeWithLong(idMapper.applyAsLong(value), value);
    }

    @Override
    public Mono<Void> storeWithLong(Mono<LongObjTuple2<V>> entry) {
        return connection.storeWithLong(entry);
    }

    /**
     * Simplified version of {@link #storeWithLong(Mono)} which doesn't require a key.
     *
     * @param entry The mono providing a value to store.
     * @return A mono to signal the completion of the storage of a value.
     *
     * @see #storeWithLong(Mono)
     */
    public Mono<Void> storeValue(Mono<V> entry) {
        return connection.storeWithLong(entry.map(it -> LongObjTuple2.of(idMapper.applyAsLong(it), it)));
    }

    @Override
    public Mono<Void> storeWithLong(Iterable<LongObjTuple2<V>> entries) {
        return connection.storeWithLong(entries);
    }

    /**
     * Simplified version of {@link #storeWithLong(Iterable)} which doesn't require keys.
     *
     * @param entries The iterable providing values to store.
     * @return A mono to signal the completion of the storage of the values.
     *
     * @see #storeWithLong(Iterable)
     */
    public Mono<Void> storeValues(Iterable<V> entries) {
        return connection.storeWithLong(new MappingIterable<>(it -> LongObjTuple2.of(idMapper.applyAsLong(it), it), entries));
    }

    @Override
    public Mono<Void> storeWithLong(Flux<LongObjTuple2<V>> entryStream) {
        return connection.storeWithLong(entryStream);
    }

    /**
     * Simplified version of {@link #storeWithLong(Flux)} which doesn't require keys.
     *
     * @param entryStream The flux providing values to store.
     * @return A mono to signal the completion of the storage of the values.
     *
     * @see #storeWithLong(Flux)
     */
    public Mono<Void> storeValues(Flux<V> entryStream) {
        return connection.storeWithLong(entryStream.map(it -> LongObjTuple2.of(idMapper.applyAsLong(it), it)));
    }

    @Override
    public Mono<V> find(long id) {
        return connection.find(id);
    }

    @Override
    public Mono<V> find(Mono<Long> id) {
        return connection.find(id);
    }

    @Override
    public Mono<Boolean> exists(long id) {
        return connection.exists(id);
    }

    @Override
    public Mono<Boolean> exists(Mono<Long> id) {
        return connection.exists(id);
    }

    @Override
    public Mono<Boolean> exists(Flux<Long> ids) {
        return connection.exists(ids);
    }

    @Override
    public Flux<V> findInRange(long start, long end) {
        return connection.findInRange(start, end);
    }

    @Override
    public Flux<V> findAll(Iterable<Long> ids) {
        return connection.findAll(ids);
    }

    @Override
    public Flux<V> findAll(Flux<Long> ids) {
        return connection.findAll(ids);
    }

    @Override
    public Mono<Void> delete(long id) {
        return connection.delete(id);
    }

    @Override
    public Mono<Void> delete(Mono<Long> id) {
        return connection.delete(id);
    }

    @Override
    public Mono<Void> delete(Flux<Long> ids) {
        return connection.delete(ids);
    }

    @Override
    public Mono<Void> delete(LongObjTuple2<V> entry) {
        return connection.delete(entry);
    }

    @Override
    public Mono<Void> deleteInRange(long start, long end) {
        return connection.deleteInRange(start, end);
    }

    /**
     * Simplified version of {@link #delete(long)} which doesn't require a key.
     *
     * @param entry The value to delete.
     * @return A mono to signal the completion of the deletion of a value.
     *
     * @see #delete(LongObjTuple2)
     */
    public Mono<Void> deleteValue(V entry) {
        return connection.delete(LongObjTuple2.of(idMapper.applyAsLong(entry), entry));
    }

    @Override
    public Mono<Void> deleteAllWithLongs(Iterable<LongObjTuple2<V>> entries) {
        return connection.deleteAllWithLongs(entries);
    }

    /**
     * Simplified version of {@link #deleteAllWithLongs(Iterable)} which doesn't require keys.
     *
     * @param entries The values to delete.
     * @return A mono to signal the completion of the deletion of values.
     *
     * @see #deleteAllWithLongs(Iterable)
     */
    public Mono<Void> deleteAllValues(Iterable<V> entries) {
        return connection.deleteAllWithLongs(new MappingIterable<>(it -> LongObjTuple2.of(idMapper.applyAsLong(it), it), entries));
    }

    @Override
    public Mono<Void> deleteAllWithLongs(Flux<LongObjTuple2<V>> entries) {
        return connection.deleteAllWithLongs(entries);
    }

    /**
     * Simplified version of {@link #deleteAllWithLongs(Flux)} which doesn't require keys.
     *
     * @param entries The values to delete.
     * @return A mono to signal the completion of the deletion of values.
     *
     * @see #deleteAllWithLongs(Flux)
     */
    public Mono<Void> deleteAllValues(Flux<V> entries) {
        return connection.deleteAllWithLongs(entries.map(it -> LongObjTuple2.of(idMapper.applyAsLong(it), it)));
    }

    @Override
    public Flux<V> findAll() {
        return connection.findAll();
    }

    @Override
    public Mono<Long> count() {
        return connection.count();
    }

    @Override
    public Mono<Void> deleteAll() {
        return connection.deleteAll();
    }
}
