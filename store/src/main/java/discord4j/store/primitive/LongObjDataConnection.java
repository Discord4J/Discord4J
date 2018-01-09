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
package discord4j.store.primitive;

import discord4j.store.DataConnection;
import discord4j.store.util.LongObjTuple2;
import discord4j.store.util.MappingIterable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public interface LongObjDataConnection<V> extends DataConnection<Long, V> {

    @Override
    default LongObjMappedDataConnection<V> withMapper(Function<V, Long> idMapper) {
        return withMapper((ToLongFunction<V>) v -> (long) idMapper.apply(v));
    }

    default LongObjMappedDataConnection<V> withMapper(ToLongFunction<V> idMapper) {
        return new LongObjMappedDataConnection<>(this, idMapper);
    }

    @Override
    default Mono<Void> store(Long key, V value) {
        return storeWithLong(key, value);
    }

    Mono<Void> storeWithLong(long key, V value);

    @Override
    default Mono<Void> store(Mono<Tuple2<Long, V>> entry) {
        return storeWithLong(entry.map(LongObjTuple2::from));
    }

    Mono<Void> storeWithLong(Mono<LongObjTuple2<V>> entry);

    @Override
    default Mono<Void> store(Iterable<Tuple2<Long, V>> entries) {
        return storeWithLong(new MappingIterable<>(LongObjTuple2::from, entries));
    }

    Mono<Void> storeWithLong(Iterable<LongObjTuple2<V>> entries);

    @Override
    default Mono<Void> store(Flux<Tuple2<Long, V>> entryStream) {
        return storeWithLong(entryStream.map(LongObjTuple2::from));
    }

    Mono<Void> storeWithLong(Flux<LongObjTuple2<V>> entryStream);

    @Override
    default Mono<V> find(Long id) {
        return find((long) id);
    }

    Mono<V> find(long id);

    @Override
    Mono<V> find(Mono<Long> id); //No way around this q.q

    @Override
    default Mono<Boolean> exists(Long id) {
        return exists((long) id);
    }

    Mono<Boolean> exists(long id);

    @Override
    Mono<Boolean> exists(Mono<Long> id); //No way around this q.q

    @Override
    Mono<Boolean> exists(Flux<Long> ids); //No way around this q.q

    @Override
    Flux<V> findAll(Iterable<Long> ids); //No way around this q.q

    @Override
    Flux<V> findAll(Flux<Long> ids); //No way around this q.q

    @Override
    default Mono<Void> delete(Long id) {
        return delete((long) id);
    }

    Mono<Void> delete(long id);

    @Override
    Mono<Void> delete(Mono<Long> id); //No way around this q.q

    @Override
    Mono<Void> delete(Flux<Long> ids); //No way around this q.q

    @Override
    default Mono<Void> delete(Tuple2<Long, V> entry) {
        return delete(LongObjTuple2.from(entry));
    }

    Mono<Void> delete(LongObjTuple2<V> entry);

    @Override
    default Mono<Void> deleteAll(Iterable<Tuple2<Long, V>> entries) {
        return deleteAllWithLongs(new MappingIterable<>(LongObjTuple2::from, entries));
    }

    Mono<Void> deleteAllWithLongs(Iterable<LongObjTuple2<V>> entries);

    @Override
    default Mono<Void> deleteAll(Flux<Tuple2<Long, V>> entries) {
        return deleteAllWithLongs(entries.map(LongObjTuple2::from));
    }

    Mono<Void> deleteAllWithLongs(Flux<LongObjTuple2<V>> entries);

    @Override
    default Flux<Tuple2<Long, V>> entries() {
        return DataConnection.super.entries();
    }

    default Flux<LongObjTuple2<V>> primitiveEntries() { //TODO: Figure out how to make this more efficient (maybe)
        return entries().map(LongObjTuple2::from);
    }
}
