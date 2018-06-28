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

package discord4j.store.dsl.jvm;

import discord4j.store.Store;
import discord4j.store.dsl.Property;
import discord4j.store.dsl.QueryBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;

public class SimpleQueryBuilder<K extends Comparable<K>, T extends Serializable> implements QueryBuilder<K, T, SimpleLogicalStatementFactory<T>, SimpleLogicalStatement<T, ?>> {

    private final Store<K, T> store;
    private final SimpleLogicalStatementFactory<T> lsf;
    private final Queue<SimpleLogicalStatement<T, ?>> tests;
    @Nullable
    private final Comparator<T> sorter;
    private final int limit;

    public SimpleQueryBuilder(Store<K, T> store, Class<T> type) {
        this.store = store;
        this.lsf = new SimpleLogicalStatementFactory<>(type);
        this.tests = new LinkedList<>();
        this.sorter = null;
        this.limit = -1;
    }

    public SimpleQueryBuilder(Store<K, T> store,
                              Queue<SimpleLogicalStatement<T, ?>> currQueue,
                              @Nullable SimpleLogicalStatement<T, ?> newStmt,
                              SimpleLogicalStatementFactory<T> lsf,
                              @Nullable Comparator<T> sorter,
                              int limit) {
        this.store = store;
        this.lsf = lsf;
        this.tests = new LinkedList<>(currQueue);
        if (newStmt != null)
            this.tests.offer(newStmt);
        this.sorter = sorter;
        this.limit = limit;
    }

    @Override
    public SimpleLogicalStatementFactory<T> logicalStatementFactory() {
        return lsf;
    }

    @Override
    public QueryBuilder<K, T, SimpleLogicalStatementFactory<T>, SimpleLogicalStatement<T, ?>> filter(Function<SimpleLogicalStatementFactory<T>, SimpleLogicalStatement<T, ?>> callback) {
        SimpleLogicalStatement<T, ?> compiled = callback.apply(lsf);
        return new SimpleQueryBuilder<>(store, tests, compiled, lsf, sorter, limit);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends Comparable<V>> QueryBuilder<K, T, SimpleLogicalStatementFactory<T>, SimpleLogicalStatement<T, ?>> sortBy(Property<V> property) {
        if (!Comparable.class.isAssignableFrom(property.getType()))
            throw new RuntimeException("Property must be comparable!");
        return new SimpleQueryBuilder<>(store, tests, null, lsf, (o1, o2) -> ((Comparable<T>)o1).compareTo(o2), limit);
    }

    @Override
    public QueryBuilder<K, T, SimpleLogicalStatementFactory<T>, SimpleLogicalStatement<T, ?>> limit(int count) {
        return new SimpleQueryBuilder<>(store, tests, null, lsf, sorter, count);
    }

    Predicate<T> compileStatements() {
        return t -> {
            for (SimpleLogicalStatement<T, ?> stmt : tests) {
                if (!stmt.test(t))
                    return false;
            }
            return true;
        };
    }

    // Naive impls :eyes:

    @Override
    public Mono<T> selectOne() {
        if (limit == 0)
            return Mono.empty();
        Predicate<T> tester = compileStatements();
        return store.values().filter(tester).next();
    }

    @Override
    public Flux<T> select() {
        if (limit == 0)
            return Flux.empty();
        Predicate<T> tester = compileStatements();
        Flux<T> query = store.values().filter(tester);
        if (limit > 0)
            query = query.take(limit);
        return query;
    }

    @Override
    public Mono<?> deleteOne(Function<T, K> idExtractor) {
        return selectOne().flatMap(t -> store.delete(idExtractor.apply(t)));
    }

    @Override
    public Flux<?> delete(Function<T, K> idExtractor) {
        return select().flatMap(t -> store.delete(idExtractor.apply(t)));
    }

    @Override
    public Mono<Boolean> exists() {
        return count().map(l -> l > 0);
    }

    @Override
    public Mono<Long> count() {
        return select().count();
    }
}
