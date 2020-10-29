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

import discord4j.discordjson.json.*;
import discord4j.discordjson.json.gateway.PresenceUpdate;
import discord4j.discordjson.possible.Possible;
import reactor.util.annotation.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class ImplUtils {

    static long toLongId(String id) {
        return Long.parseUnsignedLong(id);
    }

    static Set<Long> toLongIdSet(List<String> list) {
        return Collections.synchronizedSet(list.stream().map(ImplUtils::toLongId)
                .collect(Collectors.toSet()));
    }

    static List<String> toStringIdList(Set<Long> list) {
        return Collections.unmodifiableList(list.stream().map(String::valueOf)
                .collect(Collectors.toList()));
    }

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

    static UserData userFromMember(MemberData newMember, @Nullable UserData oldUser) {
        return newMember.user();
    }

    static @Nullable UserData userFromPresence(PresenceData newPresence, @Nullable UserData oldUser) {
        if (oldUser == null) return null;
        PartialUserData partialUserData = newPresence.user();
        return UserData.builder()
                .from(oldUser)
                .username(partialUserData.username().toOptional()
                        .orElse(oldUser.username()))
                .discriminator(partialUserData.discriminator().toOptional()
                        .orElse(oldUser.discriminator()))
                .avatar(or(Possible.flatOpt(partialUserData.avatar()), oldUser::avatar))
                .build();
    }
}
