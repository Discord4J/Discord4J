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
package discord4j.core.spec;

import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Multimap;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

final class InternalSpecUtils {

    private InternalSpecUtils() {
        throw new AssertionError();
    }

    static <T> Possible<T> toPossible(@Nullable T value) {
        return value == null ? Possible.absent() : Possible.of(value);
    }

    @Nullable
    static <T, R> R mapNullable(@Nullable T value, Function<? super T, ? extends R> mapper) {
        return value != null ? mapper.apply(value) : null;
    }

    static void putIfNotNull(Map<String, Object> map, String key, @Nullable Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    static void setIfNotNull(Multimap<String, Object> map, String key, @Nullable Object value) {
        if (value != null) {
            map.set(key, value);
        }
    }

    static void setIfPresent(Multimap<String, Object> map, String key, Possible<?> value) {
        if (!value.isAbsent()) {
            map.set(key, value.get());
        }
    }

    static void addAllIfNotNull(Multimap<String, Object> map, String key, @Nullable List<Object> values) {
        if (values != null) {
            map.addAll(key, values);
        }
    }

    static <T, R> Possible<R> mapPossible(Possible<T> value, Function<? super T, ? extends R> mapper) {
        return value.isAbsent() ? Possible.absent() : Possible.of(mapper.apply(value.get()));
    }

    static <T, R> Possible<R> flatMapPossible(Possible<T> value, Function<? super T, ? extends Possible<R>> mapper) {
        return value.isAbsent() ? Possible.absent() : mapper.apply(value.get());
    }

    static <T, R> Possible<Optional<R>> mapPossibleOptional(Possible<Optional<T>> value,
                                                           Function<? super T, ? extends R> mapper) {
        return value.isAbsent() ? Possible.absent() : Possible.of(value.get().map(mapper));
    }
}
