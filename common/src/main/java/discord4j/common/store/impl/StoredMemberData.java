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

import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.PartialMemberData;
import discord4j.discordjson.json.gateway.GuildMemberUpdate;
import discord4j.discordjson.possible.Possible;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static discord4j.common.store.impl.ImplUtils.*;

/**
 * Member data with snowflakes stored as long, with mutable role ID set, and with user data taken from an existing
 * reference.
 */
class StoredMemberData {

    private final AtomicReference<StoredUserData> user;
    private final String nick_value;
    private final boolean nick_absent;
    private final Set<Long> roles;
    private final String joinedAt;
    private final String premiumSince_value;
    private final boolean premiumSince_absent;
    private final String hoistedRole;
    private final boolean deaf;
    private final boolean mute;

    StoredMemberData(MemberData original, AtomicReference<StoredUserData> user) {
        this.user = user;
        this.nick_value = Possible.flatOpt(original.nick()).orElse(null);
        this.nick_absent = original.nick().isAbsent();
        this.roles = toLongIdSet(original.roles());
        this.joinedAt = original.joinedAt();
        this.premiumSince_value = Possible.flatOpt(original.premiumSince()).orElse(null);
        this.premiumSince_absent = original.premiumSince().isAbsent();
        this.hoistedRole = original.hoistedRole().orElse(null);
        this.deaf = original.deaf();
        this.mute = original.mute();
    }

    StoredMemberData(StoredMemberData current, GuildMemberUpdate update) {
        this.user = current.user;
        user.set(new StoredUserData(update.user()));
        this.nick_value = Possible.flatOpt(update.nick()).orElse(update.nick().isAbsent() ? current.nick_value : null);
        this.nick_absent = current.nick_absent && update.nick().isAbsent();
        this.roles = toLongIdSet(update.roles());
        this.joinedAt = current.joinedAt;
        this.premiumSince_value = Possible.flatOpt(update.premiumSince())
                .orElse(update.premiumSince().isAbsent() ? current.premiumSince_value : null);
        this.premiumSince_absent = current.premiumSince_absent && update.premiumSince().isAbsent();
        this.hoistedRole = current.hoistedRole;
        this.deaf = current.deaf;
        this.mute = current.mute;
    }

    Set<Long> roleIdSet() {
        return roles;
    }

    MemberData toImmutable() {
        return MemberData.builder()
                .user(user.get().toImmutable())
                .nick(toPossibleOptional(nick_value, nick_absent))
                .roles(toStringIdList(roles))
                .joinedAt(joinedAt)
                .premiumSince(toPossibleOptional(premiumSince_value, premiumSince_absent))
                .hoistedRole(Optional.ofNullable(hoistedRole))
                .deaf(deaf)
                .mute(mute)
                .build();
    }

    PartialMemberData toPartialImmutable() {
        return PartialMemberData.builder()
                .nick(toPossibleOptional(nick_value, nick_absent))
                .roles(toStringIdList(roles))
                .joinedAt(joinedAt)
                .premiumSince(toPossibleOptional(premiumSince_value, premiumSince_absent))
                .hoistedRole(Optional.ofNullable(hoistedRole))
                .deaf(deaf)
                .mute(mute)
                .build();
    }
}
