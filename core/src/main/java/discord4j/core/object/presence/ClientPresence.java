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
package discord4j.core.object.presence;

import discord4j.discordjson.json.ActivityUpdateRequest;
import discord4j.discordjson.json.gateway.StatusUpdate;
import reactor.util.annotation.Nullable;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Presence data that can be sent to Discord.
 * <p>
 * This is as opposed to {@link Presence} which is <i>received from</i> Discord.
 * <p>
 * Each static factory corresponds to a {@link Status} with an optional {@link ClientActivity}.
 *
 * @see discord4j.core.GatewayDiscordClient#updatePresence(ClientPresence)
 * @see discord4j.core.shard.GatewayBootstrap#setInitialPresence(Function)
 * @see <a href="https://discord.com/developers/docs/topics/gateway#presence">Presence</a>
 */
public class ClientPresence {

    /**
     * Creates an {@link Status#ONLINE online} presence.
     *
     * @return An online presence.
     */
    public static ClientPresence online() {
        return of(Status.ONLINE, null);
    }

    /**
     * Creates an {@link Status#ONLINE online} presence.
     *
     * @param activity The activity to be shown.
     * @return An online presence with the given activity.
     */
    public static ClientPresence online(ClientActivity activity) {
        return of(Status.ONLINE, activity);
    }

    /**
     * Creates a {@link Status#DO_NOT_DISTURB do not disturb} presence.
     *
     * @return A do not disturb presence.
     */
    public static ClientPresence doNotDisturb() {
        return of(Status.DO_NOT_DISTURB, null);
    }

    /**
     * Creates a {@link Status#DO_NOT_DISTURB do not disturb} presence.
     *
     * @param activity The activity to be shown.
     * @return A do not disturb with the given activity.
     */
    public static ClientPresence doNotDisturb(ClientActivity activity) {
        return of(Status.DO_NOT_DISTURB, activity);
    }

    /**
     * Creates an {@link Status#IDLE idle} presence.
     *
     * @return An idle presence.
     */
    public static ClientPresence idle() {
        return of(Status.IDLE, null);
    }

    /**
     * Creates an {@link Status#IDLE idle} presence.
     *
     * @param activity The activity to be shown.
     * @return An idle presence with the given activity.
     */
    public static ClientPresence idle(ClientActivity activity) {
        return of(Status.IDLE, activity);
    }

    /**
     * Creates an {@link Status#INVISIBLE} presence.
     *
     * @return An invisible presence.
     */
    public static ClientPresence invisible() {
        return of(Status.INVISIBLE, null);
    }

    /**
     * Creates a presence with the given status and activity.
     *
     * @param status The status to be shown.
     * @param activity The activity to be shown.
     * @return A presence with the given status and activity.
     */
    public static ClientPresence of(Status status, @Nullable ClientActivity activity) {
        /*~~>*/List<ActivityUpdateRequest> activities = Optional.ofNullable(activity)
                .map(ClientActivity::getActivityUpdateRequest)
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());

        return new ClientPresence(StatusUpdate.builder()
                .status(status.getValue())
                .activities(activities)
                .afk(false) // doesn't do anything
                .since(0) // doesn't do anything
                .build());
    }

    private final StatusUpdate statusUpdate;

    private ClientPresence(StatusUpdate statusUpdate) {
        this.statusUpdate = statusUpdate;
    }

    /**
     * Converts this presence's data to an object for use by the gateway.
     *
     * @return An equivalent {@code StatusUpdate} for this presence.
     */
    public StatusUpdate getStatusUpdate() {
        return statusUpdate;
    }
}
