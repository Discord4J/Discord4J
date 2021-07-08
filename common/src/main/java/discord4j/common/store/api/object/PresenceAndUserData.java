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

package discord4j.common.store.api.object;

import discord4j.discordjson.json.PresenceData;
import discord4j.discordjson.json.UserData;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a tuple containing presence and user data, both being optional.
 */
public class PresenceAndUserData {

    private final PresenceData presenceData;
    private final UserData userData;

    private PresenceAndUserData(@Nullable PresenceData presenceData, @Nullable UserData userData) {
        this.presenceData = presenceData;
        this.userData = userData;
    }

    /**
     * Creates a new {@link PresenceAndUserData} wrapping the given presence and user data.
     *
     * @param presenceData the presence data, or null if not provided
     * @param userData     the user data, or null if not provided
     * @return a new {@link PresenceAndUserData}
     */
    public static PresenceAndUserData of(@Nullable PresenceData presenceData, @Nullable UserData userData) {
        return new PresenceAndUserData(presenceData, userData);
    }

    /**
     * Returns the presence data, if present.
     *
     * @return an optional {@link PresenceData}
     */
    public Optional<PresenceData> getPresenceData() {
        return Optional.ofNullable(presenceData);
    }

    /**
     * Returns the user data, if present.
     *
     * @return an optional {@link UserData}
     */
    public Optional<UserData> getUserData() {
        return Optional.ofNullable(userData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PresenceAndUserData)) return false;
        PresenceAndUserData that = (PresenceAndUserData) o;
        return Objects.equals(presenceData, that.presenceData) &&
                Objects.equals(userData, that.userData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(presenceData, userData);
    }

    @Override
    public String toString() {
        return "PresenceAndUserData{" +
                "presenceData=" + presenceData +
                ", userData=" + userData +
                '}';
    }
}
