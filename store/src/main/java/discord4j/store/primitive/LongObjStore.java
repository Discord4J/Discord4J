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

import discord4j.store.Store;
import discord4j.store.util.LongObjTuple2;
import discord4j.store.util.MappingIterable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.Serializable;

/**
 * This provides an active data connection to a store's data source.
 *
 * @param <V> The value type, these follow
 * <a href="https://en.wikipedia.org/wiki/JavaBeans#JavaBean_conventions">JavaBean</a> conventions.
 * @see Store
 * @see discord4j.store.util.AbsentValue
 */
public interface LongObjStore<V extends Serializable> extends Store<Long, V> {

    @Override
    default Mono<Void> save(Long key, V value) {
        return saveWithLong(key, value);
    }

    /**
     * Stores a key value pair.
     *
     * @param key The key representing the value.
     * @param value The value.
     * @return A mono which signals the completion of the storage of the pair.
     */
    Mono<Void> saveWithLong(long key, V value);

    @Override
    default Mono<Void> save(Iterable<Tuple2<Long, V>> entries) {
        return saveWithLong(new MappingIterable<>(LongObjTuple2::from, entries));
    }

    /**
     * Stores key value pairs.
     *
     * @param entries A mono providing the key value pairs.
     * @return A mono which signals the completion of the storage of the pairs.
     */
    Mono<Void> saveWithLong(Iterable<LongObjTuple2<V>> entries);

    @Override
    default Mono<Void> save(Publisher<Tuple2<Long, V>> entryStream) {
        return saveWithLong(Flux.from(entryStream).map(LongObjTuple2::from));
    }

    /**
     * Stores key value pairs.
     *
     * @param entryStream A flux providing the key value pairs.
     * @return A mono which signals the completion of the storage of the pairs.
     */
    Mono<Void> saveWithLong(Publisher<LongObjTuple2<V>> entryStream);

    @Override
    default Mono<V> find(Long id) {
        return find((long) id);
    }

    /**
     * Attempts to find the value associated with the provided id.
     *
     * @param id The id to search with.
     * @return A mono, which may or may not contain an associated object.
     */
    Mono<V> find(long id);

    @Override
    default Mono<Boolean> exists(Long id) {
        return exists((long) id);
    }

    /**
     * Checks if a value is associated with the provided id.
     *
     * @param id The id to search with.
     * @return A mono which provides true or false, depending on whether the id is associated with a value.
     */
    Mono<Boolean> exists(long id);

    @Override
    Mono<Boolean> exists(Publisher<Long> ids); //No way around this q.q

    @Override
    default Flux<V> findInRange(Long start, Long end) {
        return findInRange((long) start, (long) end);
    }

    /**
     * Retrieves all stored values with ids within a provided range.
     *
     * @param start The starting key (inclusive).
     * @param end The ending key (exclusive).
     * @return The stream of values with ids within the provided range.
     */
    Flux<V> findInRange(long start, long end);

    @Override
    Flux<V> findAll(Iterable<Long> ids); //No way around this q.q

    @Override
    Flux<V> findAll(Publisher<Long> ids); //No way around this q.q

    @Override
    default Mono<Void> delete(Long id) {
        return delete((long) id);
    }

    /**
     * Deletes a value associated with the provided id.
     *
     * @param id The id of the value to delete.
     * @return A mono which signals the completion of the deletion of the value.
     */
    Mono<Void> delete(long id);

    @Override
    Mono<Void> delete(Publisher<Long> ids); //No way around this q.q

    @Override
    default Mono<Void> delete(Tuple2<Long, V> entry) {
        return delete(LongObjTuple2.from(entry));
    }

    /**
     * Deletes a key value pair.
     *
     * @param entry The entry to delete.
     * @return A mono which signals the completion of the deletion of the value.
     */
    Mono<Void> delete(LongObjTuple2<V> entry);

    @Override
    default Mono<Void> deleteInRange(Long start, Long end) {
        return deleteInRange((long) start, (long) end);
    }

    /**
     * Deletes values within a range of ids.
     *
     * @param start The starting key (inclusive).
     * @param end The ending key (exclusive).
     * @return A mono which signals the completion of the deletion of values.
     */
    Mono<Void> deleteInRange(long start, long end);

    @Override
    default Mono<Void> deleteAll(Iterable<Tuple2<Long, V>> entries) {
        return deleteAllWithLongs(new MappingIterable<>(LongObjTuple2::from, entries));
    }

    /**
     * Deletes all provided entries.
     *
     * @param entries The entries to delete.
     * @return A mono which signals the completion of the deletion of values.
     */
    Mono<Void> deleteAllWithLongs(Iterable<LongObjTuple2<V>> entries);

    @Override
    default Mono<Void> deleteAll(Publisher<Tuple2<Long, V>> entries) {
        return deleteAllWithLongs(Flux.from(entries).map(LongObjTuple2::from));
    }

    /**
     * Deletes all provided entries.
     *
     * @param entries A stream of entries to delete.
     * @return A mono which signals the completion of the deletion of values.
     */
    Mono<Void> deleteAllWithLongs(Publisher<LongObjTuple2<V>> entries);

    @Override
    default Flux<Tuple2<Long, V>> entries() {
        return Store.super.entries();
    }

    /**
     * Gets a stream of all entries in the data source.
     *
     * @return The stream of all entries stored.
     */
    default Flux<LongObjTuple2<V>> longObjEntries() { //TODO: Figure out how to make this more efficient (maybe)
        return entries().map(LongObjTuple2::from);
    }
}
