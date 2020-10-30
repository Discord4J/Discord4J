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

import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.possible.Possible;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static discord4j.common.store.impl.ImplUtils.toPossible;

/**
 * Emoji data with snowflakes stored as long.
 */
class StoredEmojiData {

    private final long id;
    private final boolean id_null;
    private final String name;
    private final List<Long> roles_value;
    private final boolean roles_absent;
    private final AtomicReference<StoredUserData> user;
    private final boolean requireColons_value;
    private final boolean requireColons_absent;
    private final boolean managed_value;
    private final boolean managed_absent;
    private final boolean available_value;
    private final boolean available_absent;
    private final boolean animated_value;
    private final boolean animated_absent;

    StoredEmojiData(EmojiData original, @Nullable AtomicReference<StoredUserData> userRef) {
        this.id = original.id().map(ImplUtils::toLongId).orElse(-1L);
        this.id_null = !original.id().map(ImplUtils::toLongId).isPresent();
        this.name = original.name().orElse(null);
        this.roles_value = original.roles().isAbsent()
                ? null
                : original.roles().get().stream()
                        .map(ImplUtils::toLongId)
                        .collect(Collectors.toList());
        this.roles_absent = original.roles().isAbsent();
        this.user = userRef;
        this.requireColons_value = original.requireColons().toOptional().orElse(false);
        this.requireColons_absent = original.requireColons().isAbsent();
        this.managed_value = original.managed().toOptional().orElse(false);
        this.managed_absent = original.managed().isAbsent();
        this.available_value = original.available().toOptional().orElse(false);
        this.available_absent = original.available().isAbsent();
        this.animated_value = original.animated().toOptional().orElse(false);
        this.animated_absent = original.animated().isAbsent();
    }

    EmojiData toImmutable() {
        return EmojiData.builder()
                .id(Optional.ofNullable(id_null ? null : id).map(String::valueOf))
                .name(Optional.ofNullable(name))
                .roles(roles_absent
                        ? Possible.absent()
                        : Possible.of(roles_value.stream()
                                .map(String::valueOf)
                                .collect(Collectors.toList())))
                .user(user == null ? Possible.absent() : Possible.of(user.get().toImmutable()))
                .requireColons(toPossible(requireColons_value, requireColons_absent))
                .managed(toPossible(managed_value, managed_absent))
                .available(toPossible(available_value, available_absent))
                .animated(toPossible(animated_value, animated_absent))
                .build();
    }
}
