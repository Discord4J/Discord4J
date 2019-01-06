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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core.shard;

import discord4j.store.api.Store;
import discord4j.store.api.util.WithinRangePredicate;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.Serializable;
import java.util.Set;

public class ShardAwareStore<K extends Comparable<K>, V extends Serializable> implements Store<K, V> {

    private final Store<K, V> valueStore;
    private final Set<K> keySet;

    public ShardAwareStore(Store<K, V> valueStore, Set<K> keySet) {
        this.valueStore = valueStore;
        this.keySet = keySet;
    }

    @Override
    public Mono<Void> save(K key, V value) {
        return valueStore.save(key, value)
                .then(Mono.fromRunnable(() -> keySet.add(key)));
    }

    @Override
    public Mono<Void> save(Publisher<Tuple2<K, V>> entryStream) {
        return Flux.from(entryStream)
                .doOnNext(t -> valueStore.save(t.getT1(), t.getT2()))
                .doOnNext(t -> keySet.add(t.getT1()))
                .then();
    }

    @Override
    public Mono<V> find(K id) {
        return valueStore.find(id);
    }

    @Override
    public Flux<V> findInRange(K start, K end) {
        return valueStore.findInRange(start, end);
    }

    @Override
    public Mono<Long> count() {
        return valueStore.count();
    }

    @Override
    public Mono<Void> delete(K id) {
        return valueStore.delete(id)
                .then(Mono.fromRunnable(() -> keySet.remove(id)));
    }

    @Override
    public Mono<Void> delete(Publisher<K> ids) {
        return Flux.from(ids)
                .doOnNext(valueStore::delete)
                .doOnNext(keySet::remove)
                .then();
    }

    @Override
    public Mono<Void> deleteInRange(K start, K end) {
        return valueStore.keys().filter(new WithinRangePredicate<>(start, end))
                .doOnNext(valueStore::delete)
                .doOnNext(keySet::remove)
                .then();
    }

    @Override
    public Mono<Void> deleteAll() {
        return valueStore.deleteAll()
                .then(Mono.fromRunnable(keySet::clear));
    }

    @Override
    public Flux<K> keys() {
        return valueStore.keys();
    }

    @Override
    public Flux<V> values() {
        return valueStore.values();
    }

    @Override
    public Mono<Void> invalidate() {
        return delete(Flux.fromIterable(keySet))
                .then(Mono.fromRunnable(keySet::clear));
    }
}
