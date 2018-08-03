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

import discord4j.store.Store;
import discord4j.store.dsl.LogicalStatement;
import discord4j.store.dsl.LogicalStatementFactory;
import discord4j.store.dsl.QueryBuilderFactory;
import discord4j.store.dsl.jvm.SimpleQueryBuilder;
import discord4j.store.dsl.jvm.SimpleQueryBuilderFactory;
import discord4j.store.util.WithinRangePredicate;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapStore<K extends Comparable<K>, V extends Serializable> implements Store<K, V> {

    private final Map<K, V> map = new ConcurrentHashMap<>();
    private final QueryBuilderFactory<K, V> qbf = new SimpleQueryBuilderFactory<>();
    private final Class<K> key;
    private final Class<V> value;

    public MapStore(Class<K> key, Class<V> value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Class<K> getKeyType() {
        return key;
    }

    @Override
    public Class<V> getValueType() {
        return value;
    }

    @Override
    public QueryBuilderFactory<K, V> queryBuilderFactory() {
        return qbf;
    }

    @Override
    public Mono<Void> save(K key, V value) {
        return Mono.defer(() -> {
            map.put(key, value);
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> save(Publisher<Tuple2<K, V>> entryStream) {
        return Flux.from(entryStream).flatMap(tuple -> save(tuple.getT1(), tuple.getT2())).then();
    }

    @Override
    public Mono<V> find(K id) {
        return Mono.defer(() -> Mono.just(map.get(id)));
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
    public Mono<Void> delete(Publisher<K> ids) {
        return Flux.from(ids).doOnNext(map::remove).then();
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
    public Mono<Void> invalidate() {
        return Mono.empty();
    }
}
