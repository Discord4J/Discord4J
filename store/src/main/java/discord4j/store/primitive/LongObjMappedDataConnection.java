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

import discord4j.store.MappedDataConnection;
import discord4j.store.util.LongObjTuple2;
import discord4j.store.util.MappingIterable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public class LongObjMappedDataConnection<V> extends MappedDataConnection<Long, V> implements LongObjDataConnection<V> {

    private final LongObjDataConnection<V> connection;
    private final ToLongFunction<V> idMapper;

    public LongObjMappedDataConnection(LongObjDataConnection<V> connection, ToLongFunction<V> idMapper) {
        super(connection, idMapper::applyAsLong);
        this.connection = connection;
        this.idMapper = idMapper;
    }

    @Override
    public LongObjMappedDataConnection<V> withMapper(Function<V, Long> idMapper) {
        throw new UnsupportedOperationException("A mapper is already in use!");
    }

    @Override
    public LongObjMappedDataConnection<V> withMapper(ToLongFunction<V> idMapper) {
        throw new UnsupportedOperationException("A mapper is already in use!");
    }

    @Override
    public Mono<Void> storeWithLong(long key, V value) {
        return connection.storeWithLong(key, value);
    }

    public Mono<Void> storeValue(V value) {
        return connection.storeWithLong(idMapper.applyAsLong(value), value);
    }

    @Override
    public Mono<Void> storeWithLong(Mono<LongObjTuple2<V>> entry) {
        return connection.storeWithLong(entry);
    }

    public Mono<Void> storeValue(Mono<V> entry) {
        return connection.storeWithLong(entry.map(it -> LongObjTuple2.of(idMapper.applyAsLong(it), it)));
    }

    @Override
    public Mono<Void> storeWithLong(Iterable<LongObjTuple2<V>> entries) {
        return connection.storeWithLong(entries);
    }

    public Mono<Void> storeValues(Iterable<V> entries) {
        return connection.storeWithLong(new MappingIterable<>(it -> LongObjTuple2.of(idMapper.applyAsLong(it), it), entries));
    }

    @Override
    public Mono<Void> storeWithLong(Flux<LongObjTuple2<V>> entryStream) {
        return connection.storeWithLong(entryStream);
    }

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

    public Mono<Void> deleteValue(V entry) {
        return connection.delete(LongObjTuple2.of(idMapper.applyAsLong(entry), entry));
    }

    @Override
    public Mono<Void> deleteAllWithLongs(Iterable<LongObjTuple2<V>> entries) {
        return connection.deleteAllWithLongs(entries);
    }

    public Mono<Void> deleteAllValues(Iterable<V> entries) {
        return connection.deleteAllWithLongs(new MappingIterable<>(it -> LongObjTuple2.of(idMapper.applyAsLong(it), it), entries));
    }

    @Override
    public Mono<Void> deleteAllWithLongs(Flux<LongObjTuple2<V>> entries) {
        return connection.deleteAllWithLongs(entries);
    }

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
