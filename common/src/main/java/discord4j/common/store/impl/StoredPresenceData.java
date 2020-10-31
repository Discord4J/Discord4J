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

import discord4j.discordjson.json.ActivityData;
import discord4j.discordjson.json.ClientStatusData;
import discord4j.discordjson.json.PartialUserData;
import discord4j.discordjson.json.PresenceData;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Presence data with user data taken from an existing reference.
 */
class StoredPresenceData {

    private final AtomicReference<StoredUserData> ref;
    private final String status;
    private final List<ActivityData> activities;
    private final ClientStatusData clientStatus;

    StoredPresenceData(PresenceData original, AtomicReference<StoredUserData> ref) {
        this.ref = ref;
        this.status = original.status();
        this.activities = original.activities();
        this.clientStatus = original.clientStatus();
    }

    PresenceData toImmutable() {
        PartialUserData partialUser = ref.get().toPartialImmutable();
        return PresenceData.builder()
                .user(partialUser)
                .status(status)
                .activities(activities)
                .clientStatus(clientStatus)
                .build();
    }
}
