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

import discord4j.common.LogUtil;
import discord4j.store.api.Store;
import discord4j.store.api.util.WithinRangePredicate;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;

/**
 * A custom and generic {@link Store} implementation that keeps track of the shard particular entities are cached, in
 * order to allow orderly cleanup if a specific shard needs to invalidate their cache.
 *
 * @param <K> the delegate {@link Store} key type
 * @param <V> the delegate {@link Store} value type
 */
public class ShardAwareStore<K extends Comparable<K>, V> implements Store<K, V> {

    private final Store<K, V> valueStore;
    private final KeyStore<K> keyStore;

    public ShardAwareStore(Store<K, V> valueStore, KeyStore<K> keyStore) {
        this.valueStore = valueStore;
        this.keyStore = keyStore;
    }

    @Override
    public Mono<Void> save(K key, V value) {
        return Mono.subscriberContext()
                .flatMap(ctx -> valueStore.save(key, value).then(addKey(ctx, key)));
    }

    @Override
    public Mono<Void> save(Publisher<Tuple2<K, V>> entryStream) {
        return Mono.subscriberContext()
                .flatMap(ctx -> Flux.from(entryStream)
                        .flatMap(t -> valueStore.save(t.getT1(), t.getT2()).then(addKey(ctx, t.getT1())))
                        .then());
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
        return Mono.subscriberContext()
                .flatMap(ctx -> valueStore.delete(id).then(removeKey(ctx, id)));
    }

    @Override
    public Mono<Void> delete(Publisher<K> ids) {
        return Mono.subscriberContext()
                .flatMap(ctx -> Flux.from(ids)
                        .flatMap(id -> valueStore.delete(id).then(removeKey(ctx, id)))
                        .then());
    }

    @Override
    public Mono<Void> deleteInRange(K start, K end) {
        return Mono.subscriberContext()
                .flatMap(ctx -> valueStore.keys().filter(new WithinRangePredicate<>(start, end))
                        .flatMap(id -> valueStore.delete(id).then(removeKey(ctx, id)))
                        .then());
    }

    @Override
    public Mono<Void> deleteAll() {
        return Mono.subscriberContext()
                .flatMap(ctx -> valueStore.deleteAll().then(clearKeys(ctx)));
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
        return Mono.subscriberContext()
                .flatMap(ctx -> delete(getKeys(ctx)).then(clearKeys(ctx)));
    }

    private Mono<Void> addKey(Context ctx, K key) {
        return Mono.fromRunnable(() -> ctx.<Integer>getOrEmpty(LogUtil.KEY_SHARD_ID)
                .ifPresent(id -> keyStore.add(id, key)));
    }

    private Mono<Void> removeKey(Context ctx, K key) {
        return Mono.fromRunnable(() -> ctx.<Integer>getOrEmpty(LogUtil.KEY_SHARD_ID)
                .ifPresent(id -> keyStore.remove(id, key)));
    }

    private Mono<Void> clearKeys(Context ctx) {
        return Mono.fromRunnable(() -> ctx.<Integer>getOrEmpty(LogUtil.KEY_SHARD_ID)
                .ifPresent(keyStore::clear));
    }

    private Flux<K> getKeys(Context ctx) {
        return Flux.defer(() -> ctx.<Integer>getOrEmpty(LogUtil.KEY_SHARD_ID)
                .map(id -> Flux.fromIterable(keyStore.keys(id)))
                .orElseGet(Flux::empty));
    }

    @Override
    public String toString() {
        return "ShardAwareStore{"
                + "valueStore=" + valueStore
                + '}';
    }
}
