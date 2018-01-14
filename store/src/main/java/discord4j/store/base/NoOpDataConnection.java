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
    public Mono<Void> store(K key, V value) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> store(Mono<Tuple2<K, V>> entry) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> store(Iterable<Tuple2<K, V>> entries) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> store(Flux<Tuple2<K, V>> entryStream) {
        return Mono.empty();
    }

    @Override
    public Mono<V> find(K id) {
        return Mono.empty();
    }

    @Override
    public Mono<V> find(Mono<K> id) {
        return Mono.empty();
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
    public Mono<Boolean> exists(Flux<K> ids) {
        return Mono.just(false);
    }

    @Override
    public Flux<V> findAll() {
        return Flux.empty();
    }

    @Override
    public Flux<V> findAll(Iterable<K> ids) {
        return Flux.empty();
    }

    @Override
    public Flux<V> findAll(Flux<K> ids) {
        return Flux.empty();
    }

    @Override
    public Mono<Long> count() {
        return Mono.just(0L);
    }

    @Override
    public Mono<Void> delete(K id) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> delete(Mono<K> id) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> delete(Flux<K> ids) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> delete(Tuple2<K, V> entry) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteAll(Iterable<Tuple2<K, V>> entries) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteAll(Flux<Tuple2<K, V>> entries) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteAll() {
        return Mono.empty();
    }

    @Override
    public Flux<K> keys() {
        return Flux.empty();
    }

    @Override
    public Flux<V> values() {
        return Flux.empty();
    }
}
