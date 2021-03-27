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

import discord4j.discordjson.json.ActivityUpdateRequest;
import discord4j.discordjson.json.PresenceData;
import discord4j.discordjson.json.gateway.StatusUpdate;
import discord4j.discordjson.possible.Possible;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Presence is the current state of a user on a guild.
 * <p>
 * Factories exist to build an {@link StatusUpdate} object to update the bot's status:
 * <ul>
 *     <li>{@link Presence#online()} and {@link Presence#online(ActivityUpdateRequest)}</li>
 *     <li>{@link Presence#idle()} and {@link Presence#idle(ActivityUpdateRequest)}</li>
 *     <li>{@link Presence#doNotDisturb()} and {@link Presence#doNotDisturb(ActivityUpdateRequest)}</li>
 *     <li>{@link Presence#invisible()}</li>
 * </ul>
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#presence">Presence</a>
 */
public final class Presence {

    /**
     * Create a status update to display an online status.
     *
     * @return a {@link StatusUpdate} for the ONLINE status
     */
    public static StatusUpdate online() {
        return StatusUpdate.builder()
                .status(Status.ONLINE.getValue())
                .activities(Optional.empty())
                .afk(false)
                .since(Optional.empty())
                .build();
    }

    /**
     * Create a status update to display an online status along with a custom activity.
     * <p>
     * Factories exist to build an {@link ActivityUpdateRequest} object for {@link StatusUpdate}:
     * <ul>
     *     <li>{@link Activity#listening(String)}</li>
     *     <li>{@link Activity#playing(String)}</li>
     *     <li>{@link Activity#streaming(String, String)}</li>
     *     <li>{@link Activity#watching(String)}</li>
     *     <li>{@link Activity#competing(String)}</li>
     * </ul>
     *
     * @return a {@link StatusUpdate} for the ONLINE status
     */
    public static StatusUpdate online(ActivityUpdateRequest activity) {
        return StatusUpdate.builder()
                .status(Status.ONLINE.getValue())
                .activities(Collections.singletonList(activity))
                .afk(false)
                .since(Optional.empty())
                .build();
    }

    /**
     * Create a status update to display a do-not-disturb status.
     *
     * @return a {@link StatusUpdate} for the DO_NOT_DISTURB status
     */
    public static StatusUpdate doNotDisturb() {
        return StatusUpdate.builder()
                .status(Status.DO_NOT_DISTURB.getValue())
                .activities(Optional.empty())
                .afk(false)
                .since(Optional.empty())
                .build();
    }

    /**
     * Create a status update to display a do-not-disturb status along with a custom activity.
     * <p>
     * Factories exist to build an {@link ActivityUpdateRequest} object for {@link StatusUpdate}:
     * <ul>
     *     <li>{@link Activity#listening(String)}</li>
     *     <li>{@link Activity#playing(String)}</li>
     *     <li>{@link Activity#streaming(String, String)}</li>
     *     <li>{@link Activity#watching(String)}</li>
     *     <li>{@link Activity#competing(String)}</li>
     * </ul>
     *
     * @return a {@link StatusUpdate} for the DO_NOT_DISTURB status
     */
    public static StatusUpdate doNotDisturb(ActivityUpdateRequest activity) {
        return StatusUpdate.builder()
                .status(Status.DO_NOT_DISTURB.getValue())
                .activities(Collections.singletonList(activity))
                .afk(false)
                .since(Optional.empty())
                .build();
    }

    /**
     * Create a status update to display an idle status.
     *
     * @return a {@link StatusUpdate} for the IDLE status
     */
    public static StatusUpdate idle() {
        return StatusUpdate.builder()
                .status(Status.IDLE.getValue())
                .activities(Optional.empty())
                .afk(true)
                .since(Instant.now().toEpochMilli())
                .build();
    }

    /**
     * Create a status update to display an idle status along with a custom activity.
     * <p>
     * Factories exist to build an {@link ActivityUpdateRequest} object for {@link StatusUpdate}:
     * <ul>
     *     <li>{@link Activity#listening(String)}</li>
     *     <li>{@link Activity#playing(String)}</li>
     *     <li>{@link Activity#streaming(String, String)}</li>
     *     <li>{@link Activity#watching(String)}</li>
     *     <li>{@link Activity#competing(String)}</li>
     * </ul>
     *
     * @return a {@link StatusUpdate} for the IDLE status
     */
    public static StatusUpdate idle(ActivityUpdateRequest activity) {
        return StatusUpdate.builder()
                .status(Status.IDLE.getValue())
                .activities(Collections.singletonList(activity))
                .afk(true)
                .since(Instant.now().toEpochMilli())
                .build();
    }

    /**
     * Create a status update to display an invisible status.
     *
     * @return a {@link StatusUpdate} for the INVISIBLE status
     */
    public static StatusUpdate invisible() {
        return StatusUpdate.builder()
                .status(Status.INVISIBLE.getValue())
                .activities(Optional.empty())
                .afk(false)
                .since(Optional.empty())
                .build();
    }

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
            default: throw new AssertionError("Unhandled platform " + platform);
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
    public List<Activity> getActivities() {
        return data.activities().stream().map(Activity::new).collect(Collectors.toList());
    }

    /**
     * Convert a received {@link Presence} into a {@link StatusUpdate} that can be used for sending an update.
     *
     * @return a {@link StatusUpdate} with the contents of the current {@link Presence} data.
     */
    public StatusUpdate asStatusUpdate() {
        return StatusUpdate.builder()
                .status(data.status())
                .activities(data.activities()
                        .stream()
                        .<ActivityUpdateRequest>map(activity ->  ActivityUpdateRequest.builder()
                                .from(activity)
                                .url(Possible.flatOpt(activity.url()))
                                .build())
                        .map(Collections::singletonList)
                        .findFirst())
                .afk(data.status().equals(Status.IDLE.getValue()))
                .since(data.status().equals(Status.IDLE.getValue()) ?
                        Optional.of(Instant.now().toEpochMilli()) : Optional.empty())
                .build();
    }

    @Override
    public String toString() {
        return "Presence{" +
                "data=" + data +
                '}';
    }
}
