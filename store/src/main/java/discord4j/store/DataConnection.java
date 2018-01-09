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

    Mono<Optional<V>> store(K key, V value);

    Mono<Optional<V>> store(Mono<Tuple2<K, V>> entry);

    Flux<Optional<V>> store(Iterable<Tuple2<K, V>> entries);

    Flux<Optional<V>> store(Flux<Tuple2<K, V>> entryStream);

    Mono<Optional<V>> find(K id);

    Mono<Optional<V>> find(Mono<K> id);

    Mono<Boolean> exists(K id);

    Mono<Boolean> exists(Mono<K> id);

    Flux<Boolean> exists(Flux<K> ids);

    Flux<V> findAll();

    Flux<Optional<V>> findAll(Iterable<K> ids);

    Flux<Optional<V>> findAll(Flux<K> ids);

    Mono<Long> count();

    Mono<Optional<V>> delete(K id);

    Mono<Optional<V>> delete(Mono<K> id);

    Flux<Optional<V>> delete(Flux<K> ids);

    Mono<Optional<V>> delete(Tuple2<K, V> entry);

    Flux<Optional<V>> deleteAll(Iterable<Tuple2<K, V>> entries);

    Flux<Optional<V>> deleteAll(Flux<Tuple2<K, V>> entries);

    Flux<V> deleteAll();

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
