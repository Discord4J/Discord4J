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
package discord4j.store.service;

import discord4j.store.ConnectionSource;
import discord4j.store.StoreConnection;
import discord4j.store.util.WithinRangePredicate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class MapStore<K extends Comparable<K>, V> implements ConnectionSource<K, V> {

    private final Map<K, V> map = new ConcurrentHashMap<>();
    private final AtomicReference<MapStoreStoreConnection> lock = new AtomicReference<>();

    @Override
    public Mono<? extends StoreConnection<K, V>> getConnection(boolean lock) {
        Mono<MapStoreStoreConnection> mono = Mono.defer(() -> Mono.just(new MapStoreStoreConnection(lock)));
        if (this.lock.get() != null)
            mono = mono.zipWith(Mono.just(this.lock.get()))
                    .delayUntil(tuple -> tuple.getT2().signaler)
                    .map(Tuple2::getT1);
        if (lock)
            mono = mono.doOnNext(MapStore.this.lock::set);
        return mono;
    }

    private final class MapStoreStoreConnection implements StoreConnection<K, V> {

        final Mono<Void> signaler;
        final AtomicReference<Runnable> callback = new AtomicReference<>();

        private MapStoreStoreConnection(boolean lock) {
            if (lock) {
                signaler = Mono.create(sink -> callback.set(sink::success));
            } else {
                signaler = Mono.empty();
            }
        }

        void unlock() {
            if (callback.get() != null)
                callback.get().run();
        }

        @Override
        public Mono<Void> store(K key, V value) {
            return Mono.defer(() -> {
                map.put(key, value);
                return Mono.empty();
            });
        }

        @Override
        public Mono<Void> store(Mono<Tuple2<K, V>> entry) {
            return entry.flatMap(tuple -> store(tuple.getT1(), tuple.getT2()));
        }

        @Override
        public Mono<Void> store(Iterable<Tuple2<K, V>> entries) {
            return Flux.fromIterable(entries).flatMap(tuple -> store(tuple.getT1(), tuple.getT2())).then();
        }

        @Override
        public Mono<Void> store(Flux<Tuple2<K, V>> entryStream) {
            return entryStream.flatMap(tuple -> store(tuple.getT1(), tuple.getT2())).then();
        }

        @Override
        public Mono<V> find(K id) {
            return Mono.defer(() -> Mono.just(map.get(id)));
        }

        @Override
        public Mono<V> find(Mono<K> id) {
            return id.map(map::get);
        }

        @Override
        public Mono<Boolean> exists(K id) {
            return Mono.defer(() -> Mono.just(map.containsKey(id)));
        }

        @Override
        public Mono<Boolean> exists(Mono<K> id) {
            return id.map(map::containsKey);
        }

        @Override
        public Mono<Boolean> exists(Flux<K> ids) {
            return ids.all(map::containsKey);
        }

        @Override
        public Flux<V> findAll() {
            return Flux.defer(() -> Flux.fromIterable(map.values()));
        }

        @Override
        public Flux<V> findAll(Iterable<K> ids) {
            return Flux.defer(() -> Flux.fromIterable(ids)).map(map::get);
        }

        @Override
        public Flux<V> findAll(Flux<K> ids) {
            return ids.map(map::get);
        }

        @Override
        public Flux<V> findInRange(K start, K end) {
            WithinRangePredicate<K> predicate = new WithinRangePredicate<>(start, end);
            return Flux.defer(() -> Flux.fromIterable(map.entrySet()))
                    .filter(entry -> predicate.test(entry.getKey()))
                    .map(Map.Entry::getValue);
        }

        @Override
        public Mono<Long> count() {
            return Mono.defer(() -> Mono.just((long) map.size()));
        }

        @Override
        public Mono<Void> delete(K id) {
            return Mono.defer(() -> {
                map.remove(id);
                return Mono.empty();
            });
        }

        @Override
        public Mono<Void> delete(Mono<K> id) {
            return id.doOnNext(map::remove).then();
        }

        @Override
        public Mono<Void> delete(Flux<K> ids) {
            return ids.doOnNext(map::remove).then();
        }

        @Override
        public Mono<Void> delete(Tuple2<K, V> entry) {
            return Mono.defer(() -> {
                map.remove(entry.getT1(), entry.getT2());
                return Mono.empty();
            });
        }

        @Override
        public Mono<Void> deleteInRange(K start, K end) {
            WithinRangePredicate<K> predicate = new WithinRangePredicate<>(start, end);
            return Flux.defer(() -> Flux.fromIterable(map.keySet()))
                    .filter(predicate)
                    .doOnNext(map::remove)
                    .then();
        }

        @Override
        public Mono<Void> deleteAll(Iterable<Tuple2<K, V>> entries) {
            return Flux.fromIterable(entries).doOnNext(entry -> map.remove(entry.getT1(), entry.getT2())).then();
        }

        @Override
        public Mono<Void> deleteAll(Flux<Tuple2<K, V>> entries) {
            return entries.doOnNext(entry -> map.remove(entry.getT1(), entry.getT2())).then();
        }

        @Override
        public Mono<Void> deleteAll() {
            return Mono.defer(() -> {
                map.clear();
                return Mono.empty();
            });
        }

        @Override
        public Flux<K> keys() {
            return Flux.defer(() -> Flux.fromIterable(map.keySet()));
        }

        @Override
        public Flux<V> values() {
            return Flux.defer(() -> Flux.fromIterable(map.values()));
        }

        @Override
        public void close() throws RuntimeException {
            unlock();
        }
    }
}
