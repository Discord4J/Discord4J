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
package discord4j.store.base.primitive;

import discord4j.store.primitive.LongObjDataConnection;
import discord4j.store.util.LongObjTuple2;
import discord4j.store.util.MappingIterable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class NoOpLongObjDataConnection<V> implements LongObjDataConnection<V> {

    @Override
    public Mono<Optional<V>> storeWithLong(long key, V value) {
        return Mono.just(Optional.empty());
    }

    @Override
    public Mono<Optional<V>> storeWithLong(Mono<LongObjTuple2<V>> entry) {
        return Mono.just(Optional.empty());
    }

    @Override
    public Flux<Optional<V>> storeWithLong(Iterable<LongObjTuple2<V>> entries) {
        return Flux.fromIterable(new MappingIterable<>(it -> Optional.empty(), entries));
    }

    @Override
    public Flux<Optional<V>> storeWithLong(Flux<LongObjTuple2<V>> entryStream) {
        return Flux.from(entryStream).map(it -> Optional.empty());
    }

    @Override
    public Mono<Optional<V>> find(long id) {
        return Mono.just(Optional.empty());
    }

    @Override
    public Mono<Optional<V>> find(Mono<Long> id) {
        return Mono.just(Optional.empty());
    }

    @Override
    public Mono<Boolean> exists(long id) {
        return Mono.just(false);
    }

    @Override
    public Mono<Boolean> exists(Mono<Long> id) {
        return Mono.just(false);
    }

    @Override
    public Flux<Boolean> exists(Flux<Long> ids) {
        return Flux.from(ids).map(it -> false);
    }

    @Override
    public Flux<V> findAll() {
        return Flux.empty();
    }

    @Override
    public Flux<Optional<V>> findAll(Iterable<Long> ids) {
        return Flux.fromIterable(new MappingIterable<>(it -> Optional.empty(), ids));
    }

    @Override
    public Flux<Optional<V>> findAll(Flux<Long> ids) {
        return Flux.from(ids).map(it -> Optional.empty());
    }

    @Override
    public Mono<Long> count() {
        return Mono.just(0L);
    }

    @Override
    public Mono<Optional<V>> delete(long id) {
        return Mono.just(Optional.empty());
    }

    @Override
    public Mono<Optional<V>> delete(Mono<Long> id) {
        return Mono.just(Optional.empty());
    }

    @Override
    public Flux<Optional<V>> delete(Flux<Long> ids) {
        return Flux.from(ids).map(it -> Optional.empty());
    }

    @Override
    public Flux<V> deleteAll() {
        return Flux.empty();
    }

    @Override
    public Flux<Long> keys() {
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

    @Override
    public Mono<Optional<V>> delete(LongObjTuple2<V> entry) {
        return Mono.just(Optional.empty());
    }

    @Override
    public Flux<Optional<V>> deleteAllWithLongs(Iterable<LongObjTuple2<V>> entries) {
        return Flux.fromIterable(new MappingIterable<>(it -> Optional.empty(), entries));
    }

    @Override
    public Flux<Optional<V>> deleteAllWithLongs(Flux<LongObjTuple2<V>> entries) {
        return Flux.from(entries).map(it -> Optional.empty());
    }
}
