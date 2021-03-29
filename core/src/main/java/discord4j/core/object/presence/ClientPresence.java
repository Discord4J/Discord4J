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

import discord4j.discordjson.json.gateway.StatusUpdate;

import java.time.Instant;
import java.util.Collections;
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
 */
public class ClientPresence {

    public static ClientPresence online() {
        return new ClientPresence(StatusUpdate.builder()
                .status(Status.ONLINE.getValue())
                .activities(Optional.empty())
                .afk(false)
                .since(Optional.empty())
                .build());
    }

    public static ClientPresence online(ClientActivity activity) {
        return new ClientPresence(StatusUpdate.builder()
                .status(Status.ONLINE.getValue())
                .activities(Collections.singletonList(activity.getActivityUpdateRequest()))
                .afk(false)
                .since(Optional.empty())
                .build());
    }

    public static ClientPresence doNotDisturb() {
        return new ClientPresence(StatusUpdate.builder()
                .status(Status.DO_NOT_DISTURB.getValue())
                .activities(Optional.empty())
                .afk(false)
                .since(Optional.empty())
                .build());
    }

    public static ClientPresence doNotDisturb(ClientActivity activity) {
        return new ClientPresence(StatusUpdate.builder()
                .status(Status.DO_NOT_DISTURB.getValue())
                .activities(Collections.singletonList(activity.getActivityUpdateRequest()))
                .afk(false)
                .since(Optional.empty())
                .build());
    }

    public static ClientPresence idle() {
        return new ClientPresence(StatusUpdate.builder()
                .status(Status.IDLE.getValue())
                .activities(Optional.empty())
                .afk(true)
                .since(Instant.now().toEpochMilli())
                .build());
    }

    public static ClientPresence idle(ClientActivity activity) {
        return new ClientPresence(StatusUpdate.builder()
                .status(Status.IDLE.getValue())
                .activities(Collections.singletonList(activity.getActivityUpdateRequest()))
                .afk(true)
                .since(Instant.now().toEpochMilli())
                .build());
    }

    public static ClientPresence invisible() {
        return new ClientPresence(StatusUpdate.builder()
                .status(Status.INVISIBLE.getValue())
                .activities(Optional.empty())
                .afk(false)
                .since(Optional.empty())
                .build());
    }

    private final StatusUpdate statusUpdate;

    private ClientPresence(StatusUpdate statusUpdate) {
        this.statusUpdate = statusUpdate;
    }

    public StatusUpdate getStatusUpdate() {
        return statusUpdate;
    }
}
