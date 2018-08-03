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
package discord4j.store.noop.primitive;

import discord4j.store.dsl.LogicalStatement;
import discord4j.store.dsl.LogicalStatementFactory;
import discord4j.store.dsl.QueryBuilderFactory;
import discord4j.store.dsl.jvm.SimpleQueryBuilderFactory;
import discord4j.store.noop.NoOpStore;
import discord4j.store.primitive.LongObjStore;
import discord4j.store.util.LongObjTuple2;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;

/**
 * Data connection implementation which does nothing.
 *
 * @see NoOpLongObjStore
 * @see NoOpStore
 */
public class NoOpLongObjStore<V extends Serializable> implements LongObjStore<V> {

    private final Class<V> type;

    public NoOpLongObjStore(Class<V> type) {
        this.type = type;
    }

    @Override
    public Mono<Void> saveWithLong(long key, V value) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> saveWithLong(Publisher<LongObjTuple2<V>> entryStream) {
        return Mono.empty();
    }

    @Override
    public Mono<V> find(long id) {
        return Mono.empty();
    }

    @Override
    public Flux<V> findInRange(long start, long end) {
        return Flux.empty();
    }

    @Override
    public Class<V> getValueType() {
        return type;
    }

    @Override
    public QueryBuilderFactory<Long, V> queryBuilderFactory() {
        return new SimpleQueryBuilderFactory<>();
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
    public Mono<Void> delete(Publisher<Long> ids) {
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
    public Mono<Void> invalidate() {
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteInRange(long start, long end) {
        return Mono.empty();
    }
}
