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
 * An implementation of {@link LongObjStore} which is a data connection that delegates to another, generic
 * one.
 *
 * @see LongObjStore
 */
public class ForwardingStore<V extends Serializable> implements LongObjStore<V> {

    private final Store<Long, V> toForward;

    /**
     * Constructs the data connection.
     *
     * @param toForward The generic data connection to forward to.
     */
    public ForwardingStore(Store<Long, V> toForward) {
        this.toForward = toForward;
    }

    /**
     * Gets the original, generic data connection.
     *
     * @return The original data connection.
     */
    protected Store<Long, V> getOriginal() {
        return toForward;
    }

    @Override
    public Mono<Void> save(Long key, V value) {
        return toForward.save(key, value);
    }

    @Override
    public Mono<Void> saveWithLong(long key, V value) {
        return this.save(key, value);
    }

    @Override
    public Mono<Void> save(Iterable<Tuple2<Long, V>> entries) {
        return toForward.save(entries);
    }

    @Override
    public Mono<Void> saveWithLong(Iterable<LongObjTuple2<V>> entries) {
        return this.save(new MappingIterable<>(LongObjTuple2::convert, entries));
    }

    @Override
    public Mono<Void> save(Publisher<Tuple2<Long, V>> entryStream) {
        return toForward.save(entryStream);
    }

    @Override
    public Mono<Void> saveWithLong(Publisher<LongObjTuple2<V>> entryStream) {
        return this.save(Flux.from(entryStream).map(LongObjTuple2::convert));
    }

    @Override
    public Mono<V> find(Long id) {
        return toForward.find(id);
    }

    @Override
    public Mono<V> find(long id) {
        return this.find((Long) id);
    }

    @Override
    public Mono<Boolean> exists(Long id) {
        return toForward.exists(id);
    }

    @Override
    public Mono<Boolean> exists(long id) {
        return this.exists((Long) id);
    }

    @Override
    public Mono<Boolean> exists(Publisher<Long> ids) {
        return toForward.exists(ids);
    }

    @Override
    public Flux<V> findInRange(Long start, Long end) {
        return toForward.findInRange(start, end);
    }

    @Override
    public Flux<V> findInRange(long start, long end) {
        return this.findInRange((Long) start, (Long) end);
    }

    @Override
    public Flux<V> findAll(Iterable<Long> ids) {
        return toForward.findAll(ids);
    }

    @Override
    public Flux<V> findAll(Publisher<Long> ids) {
        return toForward.findAll(ids);
    }

    @Override
    public Mono<Long> count() {
        return toForward.count();
    }

    @Override
    public Mono<Void> delete(Long id) {
        return toForward.delete(id);
    }

    @Override
    public Mono<Void> delete(long id) {
        return this.delete((Long) id);
    }

    @Override
    public Mono<Void> delete(Publisher<Long> ids) {
        return toForward.delete(ids);
    }

    @Override
    public Mono<Void> delete(Tuple2<Long, V> entry) {
        return toForward.delete(entry);
    }

    @Override
    public Mono<Void> delete(LongObjTuple2<V> entry) {
        return this.delete(LongObjTuple2.convert(entry));
    }

    @Override
    public Mono<Void> deleteInRange(Long start, Long end) {
        return toForward.deleteInRange(start, end);
    }

    @Override
    public Mono<Void> deleteInRange(long start, long end) {
        return this.deleteInRange((Long) start, (Long) end);
    }

    @Override
    public Mono<Void> deleteAll(Iterable<Tuple2<Long, V>> entries) {
        return toForward.deleteAll(entries);
    }

    @Override
    public Mono<Void> deleteAllWithLongs(Iterable<LongObjTuple2<V>> entries) {
        return this.deleteAll(new MappingIterable<>(LongObjTuple2::convert, entries));
    }

    @Override
    public Mono<Void> deleteAll(Publisher<Tuple2<Long, V>> entries) {
        return toForward.deleteAll(entries);
    }

    @Override
    public Mono<Void> deleteAllWithLongs(Publisher<LongObjTuple2<V>> entries) {
        return this.deleteAll(Flux.from(entries).map(LongObjTuple2::convert));
    }

    @Override
    public Mono<Void> deleteAll() {
        return toForward.deleteAll();
    }

    @Override
    public Flux<Long> keys() {
        return toForward.keys();
    }

    @Override
    public Flux<V> values() {
        return toForward.values();
    }

    @Override
    public Flux<Tuple2<Long, V>> entries() {
        return toForward.entries();
    }

    @Override
    public Flux<LongObjTuple2<V>> longObjEntries() {
        return this.entries().map(LongObjTuple2::from);
    }
}
