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
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.json.gateway.GuildMemberUpdate;
import discord4j.discordjson.possible.Possible;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * MemberData with atomically mutable GuildMemberUpdate fields, mutable role ID set, and UserData taken from an
 * existing reference.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class StoredMemberData implements MemberData {

    private static final AtomicReferenceFieldUpdater<StoredMemberData, MemberUpdateFields> UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(StoredMemberData.class, MemberUpdateFields.class,
                    "memberUpdateFields");

    private final AtomicReference<UserData> user;
    private final String joinedAt;
    private final Optional<String> hoistedRole;
    private final boolean deaf;
    private final boolean mute;
    private volatile MemberUpdateFields memberUpdateFields;

    StoredMemberData(MemberData original, AtomicReference<UserData> user) {
        this.user = user;
        this.joinedAt = original.joinedAt();
        this.hoistedRole = original.hoistedRole();
        this.deaf = original.deaf();
        this.mute = original.mute();
        this.memberUpdateFields = new MemberUpdateFields(original);
    }

    void update(GuildMemberUpdate update) {
        for (;;) {
            MemberUpdateFields current = memberUpdateFields;
            if (!UPDATER.compareAndSet(this, current, new MemberUpdateFields(update))) {
                continue;
            }
            user.set(update.user()); // Not part of the atomic operation, user is updated independently
            return;
        }
    }

    @Override
    public UserData user() {
        return user.get();
    }

    @Override
    public Possible<Optional<String>> nick() {
        return memberUpdateFields.nick;
    }

    @Override
    public List<String> roles() {
        return ImplUtils.toStringIdList(memberUpdateFields.roles);
    }

    Set<Long> roleIdSet() {
        return memberUpdateFields.roles;
    }

    @Override
    public String joinedAt() {
        return joinedAt;
    }

    @Override
    public Possible<Optional<String>> premiumSince() {
        return memberUpdateFields.premiumSince;
    }

    @Override
    public Optional<String> hoistedRole() {
        return hoistedRole;
    }

    @Override
    public boolean deaf() {
        return deaf;
    }

    @Override
    public boolean mute() {
        return mute;
    }

    private static class MemberUpdateFields {
        private final Possible<Optional<String>> nick;
        private final Set<Long> roles;
        private final Possible<Optional<String>> premiumSince;

        private MemberUpdateFields(MemberData original) {
            this.nick = original.nick();
            this.roles = ImplUtils.toLongIdSet(original.roles());
            this.premiumSince = original.premiumSince();
        }

        private MemberUpdateFields(GuildMemberUpdate update) {
            this.nick = update.nick();
            this.roles = ImplUtils.toLongIdSet(update.roles());
            this.premiumSince = update.premiumSince();
        }
    }
}
