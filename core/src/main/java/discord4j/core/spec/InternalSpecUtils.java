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
import reactor.util.annotation.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

final class InternalSpecUtils {

    private InternalSpecUtils() {
        throw new AssertionError();
    }

    static <T> Possible<T> toPossible(@Nullable T value) {
        return value != null ? Possible.of(value) : Possible.absent();
    }

    @Nullable
    static <T, R> R mapNullable(@Nullable T value, Function<? super T, ? extends R> mapper) {
        return value != null ? mapper.apply(value) : null;
    }

    @Nullable
    static <T, R> Possible<R> mapPossible(@Nullable Possible<T> value, Function<? super T, ? extends R> mapper) {
        return mapNullable(value, possible -> possible.isAbsent() ? Possible.absent() :
                Possible.of(mapper.apply(possible.get())));
    }
    
    static void putIfNotNull(Map<String, Object> map, String key, @Nullable Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    static <T> Possible<Optional<T>> toPossibleOptional(@Nullable Possible<T> value) {
        return value == null ? Possible.of(Optional.empty()) :
                value.isAbsent() ? Possible.absent() : Possible.of(Optional.of(value.get()));
    }
}
