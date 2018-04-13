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

import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.bean.ActivityBean;
import discord4j.core.object.util.Snowflake;
import discord4j.core.util.EntityUtil;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * A Discord presence activity.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#activity-object">Activity Object</a>
 */
public final class Activity implements DiscordObject {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final ActivityBean data;

    /**
     * Constructs a {@code Activity} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Activity(final ServiceMediator serviceMediator, final ActivityBean data) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    /**
     * Gets the activity's name.
     *
     * @return The activity's name.
     */
    public String getName() {
        return data.getName();
    }

    /**
     * Gets the specific "action" for this activity.
     *
     * @return The specific "action" for this activity.
     */
    public Type getType() {
        return Type.of(data.getType());
    }

    /**
     * Gets the stream URL, if present.
     *
     * @return The stream url, if present.
     */
    public Optional<String> getStreamingUrl() {
        return Optional.ofNullable(data.getUrl());
    }

    /**
     * Gets the UNIX time (in milliseconds) of when the activity started, if present.
     *
     * @return The UNIX time (in milliseconds) of when the activity started, if present.
     */
    public Optional<Instant> getStart() {
        return Optional.ofNullable(data.getStart()).map(Instant::ofEpochMilli);
    }

    /**
     * Gets the UNIX time (in milliseconds) of when the activity ends, if present.
     *
     * @return The UNIX time (in milliseconds) of when the activity ends, if present.
     */
    public Optional<Instant> getEnd() {
        return Optional.ofNullable(data.getEnd()).map(Instant::ofEpochMilli);
    }

    /**
     * Gets the application ID for the game, if present.
     *
     * @return The application ID for the game, if present.
     */
    public Optional<Snowflake> getApplicationId() {
        return Optional.ofNullable(data.getApplicationId()).map(Snowflake::of);
    }

    /**
     * Gets what the player is currently doing, if present.
     *
     * @return What the player is currently doing, if present.
     */
    public Optional<String> getDetails() {
        return Optional.ofNullable(data.getDetails());
    }

    /**
     * Gets the user's current party status, if present.
     *
     * @return The user's current party status, if present.
     */
    public Optional<String> getState() {
        return Optional.ofNullable(data.getState());
    }

    /**
     * Gets the ID of the party, if present.
     *
     * @return The ID of the party, if present.
     */
    public Optional<String> getPartyId() {
        return Optional.ofNullable(data.getPartyId());
    }

    /**
     * Gets the party's current size, if present.
     *
     * @return The party's current size, if present.
     */
    public OptionalInt getCurrentPartySize() {
        final Integer currentPartySize = data.getCurrentPartySize();
        return (currentPartySize == null) ? OptionalInt.empty() : OptionalInt.of(currentPartySize);
    }

    /**
     * Gets the party's max size, if present.
     *
     * @return The party's max size, if present.
     */
    public OptionalInt getMaxPartySize() {
        final Integer maxPartySize = data.getMaxPartySize();
        return (maxPartySize == null) ? OptionalInt.empty() : OptionalInt.of(maxPartySize);
    }

    /**
     * Gets the ID for a large asset of the activity, usually a {@code Snowflake}, if present.
     *
     * @return The ID for a large asset of the activity, usually a {@code Snowflake}, if present.
     */
    public Optional<String> getLargeImageId() {
        return Optional.ofNullable(data.getLargeImage());
    }

    /**
     * Gets the text displayed when hovering over the large image of the activity, if present.
     *
     * @return The text displayed when hovering over the large image of the activity, if present.
     */
    public Optional<String> getLargeText() {
        return Optional.ofNullable(data.getLargeText());
    }

    /**
     * Gets the ID for a small asset of the activity, usually a {@code Snowflake}, if present.
     *
     * @return The ID for a small asset of the activity, usually a {@code Snowflake}, if present.
     */
    public Optional<String> getSmallImageId() {
        return Optional.ofNullable(data.getSmallImage());
    }

    /**
     * Gets the text displayed when hovering over the small image of the activity, if present.
     *
     * @return The text displayed when hovering over the small image of the activity, if present.
     */
    public Optional<String> getSmallText() {
        return Optional.ofNullable(data.getSmallText());
    }

    /** The type of "action" for an activity. */
    public enum Type {

        /** "Playing {name}" */
        PLAYING(0),

        /** "Streaming {name}" */
        STREAMING(1),

        /** Listening to {name} */
        LISTENING(2);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code Activity.Type}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Type(final int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the type of activity. It is guaranteed that invoking {@link #getValue()} from the returned enum will
         * equal ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of activity.
         */
        public static Type of(final int value) {
            switch (value) {
                case 0: return PLAYING;
                case 1: return STREAMING;
                case 2: return LISTENING;
                default: return EntityUtil.throwUnsupportedDiscordValue(value);
            }
        }
    }
}
