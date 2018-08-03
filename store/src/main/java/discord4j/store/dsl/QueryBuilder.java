/*
 *  This file is part of Discord4J.
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

package discord4j.store.dsl;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.function.Function;

public interface QueryBuilder<K extends Comparable<K>, T extends Serializable> {

    LogicalStatementFactory<T> logicalStatementFactory();

    QueryBuilder<K, T> filter(Function<LogicalStatementFactory<T>, ? extends LogicalStatement<T>> callback);

    <V extends Comparable<V>> QueryBuilder<K, T> sortBy(Property<V> property);

    QueryBuilder<K, T> limit(int count);

    Mono<T> selectOne();

    Flux<T> select();

    Mono<?> deleteOne(Function<T, K> idExtractor);

    Flux<?> delete(Function<T, K> idExtractor);

    Mono<Boolean> exists();

    Mono<Long> count();
}
