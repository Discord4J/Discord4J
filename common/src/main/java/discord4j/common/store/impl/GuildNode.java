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
import discord4j.discordjson.possible.Possible;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

class GuildNode {

    /**
     * Store roles for this specific guild
     */
    private final IdentityStorage<RoleData> roleStorage;

    /**
     * Store emojis for this specific guild
     */
    private final IdentityStorage<EmojiData> emojiStorage;

    /**
     * Store members for this specific guild, referencing a UserData from the user storage
     */
    private final UserRefStorage<MemberData> memberStorage;

    /**
     * Store presences for this specific guild, referencing a UserData from the user storage
     */
    private final UserRefStorage<PresenceData> presenceStorage;

    /**
     * Store voice states for this specific guild
     */
    private final IdentityStorage<VoiceStateData> voiceStateStorage;

    private final Set<Long> channelIds = ConcurrentHashMap.newKeySet();

    private volatile GuildData data;
    private volatile boolean memberListComplete;

    GuildNode(GuildData guildData, IdentityStorage<AtomicReference<UserData>> userStorage) {
        this.data = guildData;
        this.roleStorage = new IdentityStorage<>(StorageBackend.concurrentHashMap(),
                data -> LocalStoreLayout.toLongId(data.id()));
        this.emojiStorage = new IdentityStorage<>(StorageBackend.concurrentHashMap(),
                data -> data.id().map(LocalStoreLayout::toLongId).orElseThrow(AssertionError::new));
        this.memberStorage = new UserRefStorage<>(
                StorageBackend.concurrentHashMap(),
                data -> LocalStoreLayout.toLongId(data.user().id()),
                (newMember, oldUser) -> newMember.user(),
                MemberDataWithUserRef::new,
                ImmutableMemberData::copyOf,
                userStorage);
        this.presenceStorage = new UserRefStorage<>(
                StorageBackend.concurrentHashMap(),
                data -> LocalStoreLayout.toLongId(data.user().id()),
                (newPresence, oldUser) -> {
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
                },
                PresenceDataWithUserRef::new,
                ImmutablePresenceData::copyOf,
                userStorage);
        this.voiceStateStorage = new IdentityStorage<>(StorageBackend.concurrentHashMap(),
                data -> LocalStoreLayout.toLongId(data.userId()));
    }

    @Nullable GuildData getData() {
        return data;
    }

    GuildNode setData(GuildData data) {
        this.data = data;
        return this;
    }

    boolean isMemberListComplete() {
        return memberListComplete;
    }

    void completeMemberList() {
        memberListComplete = true;
    }

    IdentityStorage<RoleData> getRoleStorage() {
        return roleStorage;
    }

    IdentityStorage<EmojiData> getEmojiStorage() {
        return emojiStorage;
    }

    UserRefStorage<MemberData> getMemberStorage() {
        return memberStorage;
    }

    UserRefStorage<PresenceData> getPresenceStorage() {
        return presenceStorage;
    }

    IdentityStorage<VoiceStateData> getVoiceStateStorage() {
        return voiceStateStorage;
    }

    Set<Long> getChannelIds() {
        return channelIds;
    }

    // JDK 9
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static <T> Optional<T> or(Optional<T> first, Supplier<Optional<T>> supplier) {
        Objects.requireNonNull(supplier);
        if (first.isPresent()) {
            return first;
        } else {
            Optional<T> r = supplier.get();
            return Objects.requireNonNull(r);
        }
    }
}
