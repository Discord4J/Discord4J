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
package discord4j.core.object;

import discord4j.common.json.response.PresenceResponse;
import discord4j.core.ServiceMediator;
import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * A Discord presence.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#presence">Presence</a>
 */
public final class Presence implements DiscordObject {

    /** The serviceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final PresenceResponse presence;

    /**
     * Constructs a {@code Presence} with an associated serviceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param presence The raw data as represented by Discord, must be non-null.
     */
    public Presence(final ServiceMediator serviceMediator, final PresenceResponse presence) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.presence = Objects.requireNonNull(presence);
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    /**
     * Gets the ID of the user this presence is associated to.
     *
     * @return The ID of the user this presence is associated to.
     */
    public Snowflake getUserId() {
        return Snowflake.of(presence.getUser().getId());
    }

    /**
     * Requests to retrieve the user this presence is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} this presence is associated to.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets the activity for the user this presence is associated to, if present.
     *
     * @return The activity for the user this presence is associated to, if present.
     */
    public Optional<Activity> getActivity() {
        return Optional.ofNullable(presence.getGame()).map(game -> new Activity(serviceMediator, game));
    }

    /**
     * Gets the ID of the guild this presence is associated to, if present.
     *
     * @return The ID for the guild this presence is associated to, if present.
     */
    public Optional<Snowflake> getGuildId() {
        return Optional.ofNullable(presence.getGuildId()).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the guild this presence is associated to, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} this presence is associated to,
     * if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets the status of this presence, if possible.
     *
     * @return The status of this presence, if possible.
     */
    public Optional<Status> getStatus() {
        return Arrays.stream(Status.values())
                .filter(status -> status.value.equals(presence.getStatus()))
                .findFirst();
    }

    /** The status of a presence, indicated by a tiny colored circle next to an user's profile picture. */
    public enum Status {

        /** A status of Idle. */
        IDLE("idle"),

        /** A status of Do Not Disturb. */
        DND("dnd"),

        /** A status of Online. */
        ONLINE("online"),

        /** A status of Offline. */
        OFFLINE("offline");

        /** The underlying value as represented by Discord. */
        private final String value;

        /**
         * Constructs a {@code Presence.Status}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Status(final String value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public String getValue() {
            return value;
        }
    }
}
