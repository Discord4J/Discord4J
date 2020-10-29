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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * PresenceData with a UserData taken from an existing reference.
 */
class StoredPresenceData implements PresenceData {

    private final AtomicReference<UserData> ref;
    private final String status;
    private final List<ActivityData> activities;
    private final ClientStatusData clientStatus;

    StoredPresenceData(PresenceData original, AtomicReference<UserData> ref) {
        this.ref = ref;
        this.status = original.status();
        this.activities = original.activities();
        this.clientStatus = original.clientStatus();
    }

    @Override
    public PartialUserData user() {
        UserData userData = ref.get();
        return PartialUserData.builder()
                .username(userData.username())
                .discriminator(userData.discriminator())
                .avatar(Possible.of(userData.avatar()))
                .bot(userData.bot())
                .system(userData.system())
                .mfaEnabled(userData.mfaEnabled())
                .locale(userData.locale())
                .verified(userData.verified())
                .email(Possible.flatOpt(userData.email()).map(Possible::of).orElse(Possible.absent()))
                .flags(userData.flags())
                .premiumType(userData.premiumType())
                .publicFlags(userData.publicFlags())
                .build();
    }

    @Override
    public String status() {
        return status;
    }

    @Override
    public List<ActivityData> activities() {
        return activities;
    }

    @Override
    public ClientStatusData clientStatus() {
        return clientStatus;
    }
}
