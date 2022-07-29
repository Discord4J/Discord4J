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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.presence;

import discord4j.discordjson.json.PresenceData;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Presence is the current state of a user on a guild, received from Discord.
 * <p>
 * This is as opposed to {@link ClientPresence} which is <i>sent to</i> Discord.
 * <p>
 * A presence includes a user's {@link Activity activities} and their current {@link Status status} on a given
 * {@link Status.Platform platform}.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#presence">Presence</a>
 */
public final class Presence {

    private final PresenceData data;

    public Presence(final PresenceData data) {
        this.data = data;
    }

    /**
     * Get the user's status.
     *
     * @return The user's status.
     */
    public Status getStatus() {
        return Status.of(data.status());
    }

    /**
     * Get the user's status for the given platform, if present.
     *
     * @param platform the platform to obtain a user status
     * @return an {@link Optional} with the user's status for a given platform, or empty if not present.
     */
    public Optional<Status> getStatus(Status.Platform platform) {
        switch (platform) {
            case DESKTOP: return data.clientStatus().desktop().toOptional().map(Status::of);
            case MOBILE: return data.clientStatus().mobile().toOptional().map(Status::of);
            case WEB: return data.clientStatus().web().toOptional().map(Status::of);
            // TODO: Remove in Java 12+. The switch is exhaustive assuming Platform is not compiled separately.
            default: throw new IllegalArgumentException("Unhandled platform " + platform);
        }
    }

    /**
     * Get a user's current activity, if present.
     *
     * @return an {@link Optional} with the user's activity, or empty if not present.
     */
    public Optional<Activity> getActivity() {
        return data.activities().stream().map(Activity::new).findFirst();
    }

    /**
     * Get the user's current activities.
     *
     * @return The user's current activities.
     */
    public /*~~>*/List<Activity> getActivities() {
        return data.activities().stream().map(Activity::new).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Presence{" +
                "data=" + data +
                '}';
    }
}
