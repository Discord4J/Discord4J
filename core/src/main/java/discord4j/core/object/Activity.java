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

import discord4j.common.json.response.GameAssetsResponse;
import discord4j.common.json.response.GamePartyResponse;
import discord4j.common.json.response.GameResponse;
import discord4j.common.json.response.GameTimestampsResponse;
import discord4j.core.Client;
import discord4j.core.Shard;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * A Discord presence activity.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#activity-object">Activity Object</a>
 */
public final class Activity implements DiscordObject {

    /** The Client associated to this object. */
    private final Client client;

    /** The raw data as represented by Discord. */
    private final GameResponse activity;

    /**
     * Constructs a {@code Activity} with an associated client and Discord data.
     *
     * @param client The Client associated to this object, must be non-null.
     * @param activity The raw data as represented by Discord, must be non-null.
     */
    public Activity(final Client client, final GameResponse activity) {
        this.client = Objects.requireNonNull(client);
        this.activity = Objects.requireNonNull(activity);
    }

    @Override
    public Shard getShard() {
        return client.getShard();
    }

    /**
     * Gets the activity's name.
     *
     * @return The activity's name.
     */
    public String getName() {
        return activity.getName();
    }

    /**
     * Gets the specific "action" for this activity.
     *
     * @return The specific "action" for this activity.
     */
    public Type getType() {
        return Arrays.stream(Type.values())
                .filter(value -> value.value == activity.getType())
                .findFirst() // If this throws Discord added something
                .orElseThrow(UnsupportedOperationException::new);
    }

    /**
     * Gets the stream URL, if present.
     *
     * @return The stream url, if present.
     */
    public Optional<String> getStreamingUrl() {
        return Optional.ofNullable(activity.getUrl());
    }

    /**
     * Gets the UNIX time (in milliseconds) of when the activity started, if present.
     *
     * @return The UNIX time (in milliseconds) of when the activity started, if present.
     */
    public Optional<Instant> getStart() {
        return Optional.ofNullable(activity.getTimestamps())
                .map(GameTimestampsResponse::getStart)
                .map(Instant::ofEpochMilli);
    }

    /**
     * Gets the UNIX time (in milliseconds) of when the activity ends, if present.
     *
     * @return The UNIX time (in milliseconds) of when the activity ends, if present.
     */
    public Optional<Instant> getEnd() {
        return Optional.ofNullable(activity.getTimestamps())
                .map(GameTimestampsResponse::getStart)
                .map(Instant::ofEpochMilli);
    }

    /**
     * Gets the application ID for the game, if present.
     *
     * @return The application ID for the game, if present.
     */
    public Optional<Snowflake> getApplicationId() {
        return Optional.ofNullable(activity.getApplicationId()).map(Snowflake::of);
    }

    /**
     * Gets what the player is currently doing, if present.
     *
     * @return What the player is currently doing, if present.
     */
    public Optional<String> getDetails() {
        return Optional.ofNullable(activity.getDetails());
    }

    /**
     * Gets the user's current party status, if present.
     *
     * @return The user's current party status, if present.
     */
    public Optional<String> getState() {
        return Optional.ofNullable(activity.getState());
    }

    /**
     * Gets the ID of the party, if present.
     *
     * @return The ID of the party, if present.
     */
    public Optional<String> getPartyId() {
        return Optional.ofNullable(activity.getParty()).map(GamePartyResponse::getId);
    }

    /**
     * Gets the party's current size, if present.
     *
     * @return The party's current size, if present.
     */
    public OptionalInt getCurrentPartySize() {
        return Optional.ofNullable(activity.getParty())
                .map(GamePartyResponse::getSize)
                .map(sizes -> OptionalInt.of(sizes[0]))
                .orElse(OptionalInt.empty());
    }

    /**
     * Gets the party's max size, if present.
     *
     * @return The party's max size, if present.
     */
    public OptionalInt getMaxPartySize() {
        return Optional.ofNullable(activity.getParty())
                .map(GamePartyResponse::getSize)
                .map(sizes -> OptionalInt.of(sizes[1]))
                .orElse(OptionalInt.empty());
    }

    /**
     * Gets the ID for a large asset of the activity, usually a {@code Snowflake}, if present.
     *
     * @return The ID for a large asset of the activity, usually a {@code Snowflake}, if present.
     */
    public Optional<String> getLargeImageId() {
        return Optional.ofNullable(activity.getAssets()).map(GameAssetsResponse::getLargeImage);
    }

    /**
     * Gets the text displayed when hovering over the large image of the activity, if present.
     *
     * @return The text displayed when hovering over the large image of the activity, if present.
     */
    public Optional<String> getLargeText() {
        return Optional.ofNullable(activity.getAssets()).map(GameAssetsResponse::getLargeText);
    }

    /**
     * Gets the ID for a small asset of the activity, usually a {@code Snowflake}, if present.
     *
     * @return The ID for a small asset of the activity, usually a {@code Snowflake}, if present.
     */
    public Optional<String> getSmallImageId() {
        return Optional.ofNullable(activity.getAssets()).map(GameAssetsResponse::getSmallImage);
    }

    /**
     * Gets the text displayed when hovering over the small image of the activity, if present.
     *
     * @return The text displayed when hovering over the small image of the activity, if present.
     */
    public Optional<String> getSmallText() {
        return Optional.ofNullable(activity.getAssets()).map(GameAssetsResponse::getSmallText);
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
    }
}
