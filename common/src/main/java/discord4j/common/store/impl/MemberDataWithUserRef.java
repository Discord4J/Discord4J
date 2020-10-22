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
import discord4j.discordjson.possible.Possible;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

class MemberDataWithUserRef implements MemberData {

    private final Possible<Optional<String>> nick;
    private final List<String> roles;
    private final String joinedAt;
    private final Possible<Optional<String>> premiumSince;
    private final Optional<String> hoistedRole;
    private final boolean deaf;
    private final boolean mute;
    private final AtomicReference<UserData> ref;

    MemberDataWithUserRef(MemberData original, AtomicReference<UserData> ref) {
        this.nick = original.nick();
        this.roles = original.roles();
        this.joinedAt = original.joinedAt();
        this.premiumSince = original.premiumSince();
        this.hoistedRole = original.hoistedRole();
        this.deaf = original.deaf();
        this.mute = original.mute();
        this.ref = ref;
    }

    @Override
    public UserData user() {
        return ref.get();
    }

    @Override
    public Possible<Optional<String>> nick() {
        return nick;
    }

    @Override
    public List<String> roles() {
        return roles;
    }

    @Override
    public String joinedAt() {
        return joinedAt;
    }

    @Override
    public Possible<Optional<String>> premiumSince() {
        return premiumSince;
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
}
