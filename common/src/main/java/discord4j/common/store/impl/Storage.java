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

package discord4j.common.store.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

class Storage<N, T> {

    final Map<Long, N> map = new ConcurrentHashMap<>();
    final Function<T, Long> idGetter;
    private final Function<T, N> wrapFunc;
    private final Function<N, T> unwrapFunc;
    private final BiFunction<N, T, N> rewrapFunc;

    Storage(Function<T, Long> idGetter, Function<T, N> wrapFunc, Function<N, T> unwrapFunc,
            BiFunction<N, T, N> rewrapFunc) {
        this.idGetter = idGetter;
        this.wrapFunc = wrapFunc;
        this.unwrapFunc = unwrapFunc;
        this.rewrapFunc = rewrapFunc;
    }

    N nodeForId(long id) {
        return map.computeIfAbsent(id, k -> wrapFunc.apply(null));
    }

    Collection<N> nodes() {
        return map.values();
    }

    Optional<T> find(long id) {
        return Optional.ofNullable(map.get(id)).map(unwrapFunc);
    }

    Collection<T> findAll() {
        return map.values().stream()
                .map(unwrapFunc)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    long count() {
        return map.values().stream()
                .map(unwrapFunc)
                .filter(Objects::nonNull)
                .count();
    }

    void insert(T data) {
        update(idGetter.apply(data), ignored -> data);
    }

    @SuppressWarnings("unchecked")
    Optional<T> update(long id, UnaryOperator<T> updateFunction) {
        Capture<T> captureOld = new Capture<>();
        map.compute(id, (k, v) -> {
            if (v == null) {
                return wrapFunc.apply(updateFunction.apply(null));
            }
            T old = unwrapFunc.apply(v);
            captureOld.capture(old);
            return rewrapFunc.apply(v, updateFunction.apply(old));
        });
        return Optional.ofNullable(captureOld.get());
    }

    Optional<T> updateIfPresent(long id, UnaryOperator<T> updateFunction) {
        return update(id, old -> old == null ? null : updateFunction.apply(old));
    }

    Optional<T> delete(long id) {
        return Optional.ofNullable(map.remove(id)).map(unwrapFunc);
    }
}
