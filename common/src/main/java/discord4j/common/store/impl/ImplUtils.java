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
import reactor.core.publisher.Flux;
import reactor.util.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

class ImplUtils {

    static long toLongId(String id) {
        return Long.parseUnsignedLong(id);
    }

    static Optional<Long> idFromPossibleString(Possible<String> id) {
        return id.toOptional().map(ImplUtils::toLongId);
    }

    static Optional<Long> idFromPossibleOptionalString(Possible<Optional<String>> id) {
        return Possible.flatOpt(id).map(ImplUtils::toLongId);
    }

    static <T> Possible<T> toPossible(@Nullable T value, boolean isAbsent) {
        return isAbsent ? Possible.absent() : Possible.of(Objects.requireNonNull(value));
    }

    static <T> Possible<Optional<T>> toPossibleOptional(@Nullable T value, boolean isAbsent) {
        return toPossibleOptional(value, isAbsent, false);
    }

    static <T> Possible<Optional<T>> toPossibleOptional(@Nullable T value, boolean isAbsent, boolean forceNull) {
        return isAbsent ? Possible.absent() : Possible.of(forceNull ? Optional.empty() : Optional.ofNullable(value));
    }

    static Possible<String> toPossibleStringId(@Nullable Long id, boolean isAbsent) {
        return isAbsent ? Possible.absent() : Possible.of("" + Objects.requireNonNull(id));
    }

    static Possible<Optional<String>> toPossibleOptionalStringId(@Nullable Long id, boolean isAbsent) {
        return isAbsent ? Possible.absent() : Possible.of(Optional.ofNullable(id).map(String::valueOf));
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

    static StoredUserData userFromMember(MemberData newMember, @Nullable StoredUserData oldUser) {
        return new StoredUserData(newMember.user());
    }

    static StoredUserData userFromMessage(MessageData newMessage, @Nullable StoredUserData oldUser) {
        return new StoredUserData(newMessage.author());
    }

    static StoredUserData userFromEmoji(EmojiData newEmoji, @Nullable StoredUserData oldUser) {
        return new StoredUserData(newEmoji.user().get()); // this method is never called if absent
    }

    static @Nullable StoredUserData userFromPresence(PresenceData newPresence, @Nullable StoredUserData oldUser) {
        if (oldUser == null) return null;
        PartialUserData partialUserData = newPresence.user();
        UserData oldUserImmutable = oldUser.toImmutable();
        return new StoredUserData(UserData.builder()
                .from(oldUserImmutable)
                .username(partialUserData.username().toOptional()
                        .orElse(oldUserImmutable.username()))
                .discriminator(partialUserData.discriminator().toOptional()
                        .orElse(oldUserImmutable.discriminator()))
                .avatar(or(Possible.flatOpt(partialUserData.avatar()), oldUserImmutable::avatar))
                .build());
    }

    static @Nullable <K, V> V atomicGetAndReplace(ConcurrentMap<K, V> map, K key,
                                                  UnaryOperator<V> replaceFunction) {
        for (;;) {
            V oldV = map.get(key);
            if (oldV == null) {
                return null;
            }
            V newV = replaceFunction.apply(oldV);
            if (!map.replace(key, oldV, newV)) {
                continue;
            }
            return oldV;
        }
    }

    static <T> Flux<T> fluxFromSynchronizedIterable(Iterable<? extends T> iterable) {
        return Flux.create(sink -> {
           synchronized (iterable) {
               for (T element : iterable) {
                   sink.next(element);
               }
           }
           sink.complete();
        });
    }

}
