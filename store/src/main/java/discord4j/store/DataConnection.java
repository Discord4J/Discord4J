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
package discord4j.store;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Optional;
import java.util.function.Function;

public interface DataConnection<K, V> extends AutoCloseable {

    default MappedDataConnection<K, V> withMapper(Function<V, K> idMapper) {
        return new MappedDataConnection<>(this, idMapper);
    }

    Mono<Void> store(K key, V value);

    Mono<Void> store(Mono<Tuple2<K, V>> entry);

    Mono<Void> store(Iterable<Tuple2<K, V>> entries);

    Mono<Void> store(Flux<Tuple2<K, V>> entryStream);

    Mono<V> find(K id);

    Mono<V> find(Mono<K> id);

    Mono<Boolean> exists(K id);

    Mono<Boolean> exists(Mono<K> id);

    Mono<Boolean> exists(Flux<K> ids);

    Flux<V> findAll();

    Flux<V> findAll(Iterable<K> ids);

    Flux<V> findAll(Flux<K> ids);

    Mono<Long> count();

    Mono<Void> delete(K id);

    Mono<Void> delete(Mono<K> id);

    Mono<Void> delete(Flux<K> ids);

    Mono<Void> delete(Tuple2<K, V> entry);

    Mono<Void> deleteAll(Iterable<Tuple2<K, V>> entries);

    Mono<Void> deleteAll(Flux<Tuple2<K, V>> entries);

    Mono<Void> deleteAll();

    Flux<K> keys();

    Flux<V> values();

    default Flux<Tuple2<K, V>> entries() {
        return keys().zipWith(values());
    }

    Mono<Boolean> isConnected();

    Mono<Void> disconnect();

    @Override
    default void close() throws Exception {
        disconnect().block();
    }
}
