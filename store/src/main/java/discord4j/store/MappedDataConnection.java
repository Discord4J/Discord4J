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

import java.util.Optional;
import java.util.function.Function;

public class MappedDataConnection<K, V> implements DataConnection<K, V> {

    private final DataConnection<K, V> connection;
    private final Function<V, K> idMapper;

    public MappedDataConnection(DataConnection<K, V> connection, Function<V, K> idMapper) {
        this.connection = connection;
        this.idMapper = idMapper;
    }

    @Override
    public MappedDataConnection<K, V> withMapper(Function<V, K> idMapper) {
        throw new UnsupportedOperationException("A mapper is already in use!");
    }

    @Override
    public Mono<Optional<V>> store(K key, V value) {
        return connection.store(key, value);
    }

    public Mono<Optional<V>> storeValue(V value) {
        return connection.store(idMapper.apply(value), value);
    }

    @Override
    public Mono<Optional<V>> store(Mono<Tuple2<K, V>> entry) {
        return connection.store(entry);
    }

    public Mono<Optional<V>> storeValue(Mono<V> entry) {
        return connection.store(entry.map(it -> Tuples.of(idMapper.apply(it), it)));
    }

    @Override
    public Flux<Optional<V>> store(Iterable<Tuple2<K, V>> entries) {
        return connection.store(entries);
    }

    public Flux<Optional<V>> storeValues(Iterable<V> entries) {
        return connection.store(new MappingIterable<>(it -> Tuples.of(idMapper.apply(it), it), entries));
    }

    @Override
    public Flux<Optional<V>> store(Flux<Tuple2<K, V>> entryStream) {
        return connection.store(entryStream);
    }

    public Flux<Optional<V>> storeValues(Flux<V> entryStream) {
        return connection.store(entryStream.map(it -> Tuples.of(idMapper.apply(it), it)));
    }

    @Override
    public Mono<Optional<V>> find(K id) {
        return connection.find(id);
    }

    @Override
    public Mono<Optional<V>> find(Mono<K> id) {
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
    public Flux<Boolean> exists(Flux<K> ids) {
        return connection.exists(ids);
    }

    @Override
    public Flux<V> findAll() {
        return connection.findAll();
    }

    @Override
    public Flux<Optional<V>> findAll(Iterable<K> ids) {
        return connection.findAll(ids);
    }

    @Override
    public Flux<Optional<V>> findAll(Flux<K> ids) {
        return connection.findAll(ids);
    }

    @Override
    public Mono<Long> count() {
        return connection.count();
    }

    @Override
    public Mono<Optional<V>> delete(K id) {
        return connection.delete(id);
    }

    @Override
    public Mono<Optional<V>> delete(Mono<K> id) {
        return connection.delete(id);
    }

    @Override
    public Flux<Optional<V>> delete(Flux<K> ids) {
        return connection.delete(ids);
    }

    @Override
    public Mono<Optional<V>> delete(Tuple2<K, V> entry) {
        return connection.delete(entry);
    }

    public Mono<Optional<V>> deleteValue(V entry) {
        return connection.delete(Tuples.of(idMapper.apply(entry), entry));
    }

    @Override
    public Flux<Optional<V>> deleteAll(Iterable<Tuple2<K, V>> entries) {
        return connection.deleteAll(entries);
    }

    public Flux<Optional<V>> deleteAllValues(Iterable<V> entries) {
        return connection.deleteAll(new MappingIterable<>(it -> Tuples.of(idMapper.apply(it), it), entries));
    }

    @Override
    public Flux<Optional<V>> deleteAll(Flux<Tuple2<K, V>> entries) {
        return connection.deleteAll(entries);
    }

    public Flux<Optional<V>> deleteAllValues(Flux<V> entries) {
        return connection.deleteAll(entries.map(it -> Tuples.of(idMapper.apply(it), it)));
    }

    @Override
    public Flux<V> deleteAll() {
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
    public Mono<Boolean> isConnected() {
        return connection.isConnected();
    }

    @Override
    public Mono<Void> disconnect() {
        return connection.disconnect();
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
