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
package discord4j.store.base;

import discord4j.store.DataConnection;
import discord4j.store.util.MappingIterable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Optional;

public class NoOpDataConnection<K, V> implements DataConnection<K, V> {

    @Override
    public Mono<Optional<V>> store(K key, V value) {
        return Mono.just(Optional.empty());
    }

    @Override
    public Mono<Optional<V>> store(Mono<Tuple2<K, V>> entry) {
        return Mono.just(Optional.empty());
    }

    @Override
    public Flux<Optional<V>> store(Iterable<Tuple2<K, V>> entries) {
        return Flux.fromIterable(new MappingIterable<>(it -> Optional.empty(), entries));
    }

    @Override
    public Flux<Optional<V>> store(Flux<Tuple2<K, V>> entryStream) {
        return Flux.from(entryStream).map(it -> Optional.empty());
    }

    @Override
    public Mono<Optional<V>> find(K id) {
        return Mono.just(Optional.empty());
    }

    @Override
    public Mono<Optional<V>> find(Mono<K> id) {
        return Mono.just(Optional.empty());
    }

    @Override
    public Mono<Boolean> exists(K id) {
        return Mono.just(false);
    }

    @Override
    public Mono<Boolean> exists(Mono<K> id) {
        return Mono.just(false);
    }

    @Override
    public Flux<Boolean> exists(Flux<K> ids) {
        return Flux.from(ids).map(it -> false);
    }

    @Override
    public Flux<V> findAll() {
        return Flux.empty();
    }

    @Override
    public Flux<Optional<V>> findAll(Iterable<K> ids) {
        return Flux.fromIterable(new MappingIterable<>(it -> Optional.empty(), ids));
    }

    @Override
    public Flux<Optional<V>> findAll(Flux<K> ids) {
        return Flux.from(ids).map(it -> Optional.empty());
    }

    @Override
    public Mono<Long> count() {
        return Mono.just(0L);
    }

    @Override
    public Mono<Optional<V>> delete(K id) {
        return Mono.just(Optional.empty());
    }

    @Override
    public Mono<Optional<V>> delete(Mono<K> id) {
        return Mono.just(Optional.empty());
    }

    @Override
    public Flux<Optional<V>> delete(Flux<K> ids) {
        return Flux.from(ids).map(it -> Optional.empty());
    }

    @Override
    public Mono<Optional<V>> delete(Tuple2<K, V> entry) {
        return Mono.just(Optional.empty());
    }

    @Override
    public Flux<Optional<V>> deleteAll(Iterable<Tuple2<K, V>> entries) {
        return Flux.fromIterable(new MappingIterable<>(it -> Optional.empty(), entries));
    }

    @Override
    public Flux<Optional<V>> deleteAll(Flux<Tuple2<K, V>> entries) {
        return Flux.from(entries).map(it -> Optional.empty());
    }

    @Override
    public Flux<V> deleteAll() {
        return Flux.empty();
    }

    @Override
    public Flux<K> keys() {
        return Flux.empty();
    }

    @Override
    public Flux<V> values() {
        return Flux.empty();
    }

    @Override
    public Mono<Boolean> isConnected() {
        return Mono.just(true);
    }

    @Override
    public Mono<Void> disconnect() {
        return Mono.empty();
    }
}
