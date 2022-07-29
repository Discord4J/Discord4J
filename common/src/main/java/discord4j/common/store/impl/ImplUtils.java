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

import discord4j.discordjson.Id;
import discord4j.discordjson.json.PresenceData;
import discord4j.discordjson.json.gateway.PresenceUpdate;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ImplUtils {

    static <T> void ifNonNullDo(@Nullable T val, Consumer<? super T> action) {
        if (val != null) {
            action.accept(val);
        }
    }

    static @Nullable <T, R> R ifNonNullMap(@Nullable T val, Function<? super T, ? extends R> mapper) {
        if (val != null) {
            return mapper.apply(val);
        }
        return null;
    }

    // JDK 9
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <T> Optional<T> or(Optional<T> first, Supplier<Optional<T>> supplier) {
        Objects.requireNonNull(supplier);
        if (first.isPresent()) {
            return first;
        } else {
            Optional<T> r = supplier.get();
            return Objects.requireNonNull(r);
        }
    }

    static PresenceData createPresence(PresenceUpdate update) {
        return PresenceData.builder()
                .user(update.user())
                .status(update.status())
                .activities(update.activities())
                .clientStatus(update.clientStatus())
                .build();
    }

    static <T> /*~~>*/List<T> add(/*~~>*/List<T> list, T element) {
        return Stream.concat(list.stream(), Stream.of(element)).collect(Collectors.toList());
    }

    static <T> /*~~>*/List<T> remove(/*~~>*/List<T> list, T element) {
        return list.stream().filter(x -> !x.equals(element)).collect(Collectors.toList());
    }

    static /*~~>*/List<Id> removeAllIds(/*~~>*/List<Id> list, Set<Long> elements) {
        return list.stream().filter(x -> !elements.contains(x.asLong())).collect(Collectors.toList());
    }
}
