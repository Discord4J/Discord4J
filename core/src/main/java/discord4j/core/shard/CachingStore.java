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

import discord4j.common.annotations.Experimental;
import discord4j.store.api.Store;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Experimental
public class CachingStore<K extends Comparable<K>, V> implements Store<K, V> {

    private final Store<K, V> delegate;
    private final Function<K, Duration> ttlFactory;
    private final Map<K, Mono<V>> caches = new ConcurrentHashMap<>();

    public CachingStore(Store<K, V> delegate, Function<K, Duration> ttlFactory) {
        this.delegate = delegate;
        this.ttlFactory = ttlFactory;
    }

    @Override
    public Mono<V> find(K id) {
        return caches.computeIfAbsent(id, k -> {
            Duration ttl = ttlFactory.apply(id);
            return delegate.find(id).cache(v -> ttl, t -> Duration.ZERO, () -> Duration.ZERO);
        });
    }

    @Override
    public Flux<V> findInRange(K start, K end) {
        return delegate.findInRange(start, end);
    }

    @Override
    public Mono<Long> count() {
        return delegate.count();
    }

    @Override
    public Flux<K> keys() {
        return delegate.keys();
    }

    @Override
    public Flux<V> values() {
        return delegate.values();
    }

    @Override
    public Mono<Void> save(K key, V value) {
        return delegate.save(key, value).doOnTerminate(() -> caches.put(key, Mono.just(value)));
    }

    @Override
    public Mono<Void> save(Publisher<Tuple2<K, V>> entryStream) {
        return Flux.from(entryStream).map(t2 -> save(t2.getT1(), t2.getT2())).then();
    }

    @Override
    public Mono<Void> delete(K id) {
        return delegate.delete(id).doOnTerminate(() -> caches.remove(id));
    }

    @Override
    public Mono<Void> delete(Publisher<K> ids) {
        return Flux.from(ids).map(this::delete).then();
    }

    @Override
    public Mono<Void> deleteInRange(K start, K end) {
        return delegate.deleteInRange(start, end);
    }

    @Override
    public Mono<Void> deleteAll() {
        return delegate.deleteAll().doOnTerminate(caches::clear);
    }

    @Override
    public Mono<Void> invalidate() {
        return delegate.invalidate().doOnTerminate(caches::clear);
    }
}
