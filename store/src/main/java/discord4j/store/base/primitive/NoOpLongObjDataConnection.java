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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class NoOpLongObjDataConnection<V> implements LongObjDataConnection<V> {

    @Override
    public Mono<Void> storeWithLong(long key, V value) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> storeWithLong(Mono<LongObjTuple2<V>> entry) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> storeWithLong(Iterable<LongObjTuple2<V>> entries) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> storeWithLong(Flux<LongObjTuple2<V>> entryStream) {
        return Mono.empty();
    }

    @Override
    public Mono<V> find(long id) {
        return Mono.empty();
    }

    @Override
    public Mono<V> find(Mono<Long> id) {
        return Mono.empty();
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
    public Mono<Boolean> exists(Flux<Long> ids) {
        return Mono.just(false);
    }

    @Override
    public Flux<V> findAll() {
        return Flux.empty();
    }

    @Override
    public Flux<V> findAll(Iterable<Long> ids) {
        return Flux.empty();
    }

    @Override
    public Flux<V> findAll(Flux<Long> ids) {
        return Flux.empty();
    }

    @Override
    public Mono<Long> count() {
        return Mono.just(0L);
    }

    @Override
    public Mono<Void> delete(long id) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> delete(Mono<Long> id) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> delete(Flux<Long> ids) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteAll() {
        return Mono.empty();
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
    public Mono<Void> delete(LongObjTuple2<V> entry) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteAllWithLongs(Iterable<LongObjTuple2<V>> entries) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteAllWithLongs(Flux<LongObjTuple2<V>> entries) {
        return Mono.empty();
    }
}
