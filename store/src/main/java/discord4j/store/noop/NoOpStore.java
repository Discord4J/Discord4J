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
package discord4j.store.noop;

import discord4j.store.Store;
import discord4j.store.noop.primitive.NoOpLongObjStore;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.Serializable;

/**
 * Data connection implementation which does nothing.
 *
 * @see NoOpStore
 * @see NoOpLongObjStore
 */
public class NoOpStore<K extends Comparable<K>, V extends Serializable> implements Store<K, V> {

    @Override
    public Mono<Void> save(K key, V value) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> save(Publisher<Tuple2<K, V>> entryStream) {
        return Mono.empty();
    }

    @Override
    public Mono<V> find(K id) {
        return Mono.empty();
    }

    @Override
    public Flux<V> findInRange(K start, K end) {
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
    public Mono<Void> delete(Publisher<K> ids) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteInRange(K start, K end) {
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

    @Override
    public Mono<Void> invalidate() {
        return Mono.empty();
    }
}
