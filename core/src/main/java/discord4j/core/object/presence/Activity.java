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

import discord4j.core.object.data.stored.ActivityBean;
import discord4j.core.object.util.Snowflake;
import discord4j.core.util.EntityUtil;
import reactor.util.annotation.Nullable;

import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;

public class Activity {

    public static Activity playing(String name) {
        return new Activity(Type.PLAYING.getValue(), name, null);
    }

    public static Activity streaming(String name, String url) {
        return new Activity(Type.STREAMING.getValue(), name, url);
    }

    public static Activity listening(String name) {
        return new Activity(Type.LISTENING.getValue(), name, null);
    }

    public static Activity watching(String name) {
        return new Activity(Type.WATCHING.getValue(), name, null);
    }

    private final ActivityBean data;

    Activity(final ActivityBean data) {
        this.data = data;
    }

    private Activity(int type, String name, @Nullable String streamingUrl) {
        this.data = new ActivityBean();
        this.data.setType(type);
        this.data.setName(name);
        this.data.setUrl(streamingUrl);
    }

    /**
     * Gets the type for this activity.
     *
     * @return The type for this activity.
     */
    public Type getType() {
        return Type.of(data.getType());
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
        return Optional.ofNullable(data.getStart())
            .map(Instant::ofEpochMilli);
    }

    /**
     * Gets the UNIX time (in milliseconds) of when the activity ends, if present.
     *
     * @return The UNIX time (in milliseconds) of when the activity ends, if present.
     */
    public Optional<Instant> getEnd() {
        return Optional.ofNullable(data.getEnd())
            .map(Instant::ofEpochMilli);
    }

    /**
     * Gets the application ID for the game, if present.
     *
     * @return The application ID for the game, if present.
     */
    public Optional<Snowflake> getApplicationId() {
        return Optional.ofNullable(data.getApplicationId())
            .map(Snowflake::of);
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
    public OptionalLong getCurrentPartySize() {
        final Long currentPartySize = data.getCurrentPartySize();
        return (currentPartySize == null) ? OptionalLong.empty() : OptionalLong.of(currentPartySize);
    }

    /**
     * Gets the party's max size, if present.
     *
     * @return The party's max size, if present.
     */
    public OptionalLong getMaxPartySize() {
        final Long maxPartySize = data.getMaxPartySize();
        return (maxPartySize == null) ? OptionalLong.empty() : OptionalLong.of(maxPartySize);
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

    public Optional<String> getJoinSecret() {
        return Optional.ofNullable(data.getJoinSecret());
    }

    public Optional<String> getSpectateSecret() {
        return Optional.ofNullable(data.getSpectateSecret());
    }

    public Optional<String> getMatchSecret() {
        return Optional.ofNullable(data.getMatchSecret());
    }

    public boolean isInstance() {
        return data.getInstance();
    }

    public EnumSet<Flag> getFlags() {
        if (data.getFlags() == null) {
            return EnumSet.noneOf(Flag.class);
        }

        return Arrays.stream(Flag.values())
            .filter(f -> (data.getFlags() & f.getValue()) == f.getValue())
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(Flag.class)));
    }

    /** The type of "action" for an activity. */
    public enum Type {

        /** Unknown type **/
        UNKNOWN(-1),

        /** "Playing {name}" */
        PLAYING(0),

        /** "Streaming {name}" */
        STREAMING(1),

        /** "Listening to {name}" */
        LISTENING(2),

        /** "Watching {name}" */
        WATCHING(3);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code RichActivity.Type}.
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
                case 3: return WATCHING;
                default: return UNKNOWN;
            }
        }
    }

    public enum Flag {

        INSTANCE(1),
        JOIN(2),
        SPECTATE(4),
        JOIN_REQUEST(8),
        SYNC(16),
        PLAY(32);

        private final int value;

        Flag(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Flag of(final int value) {
            switch (value) {
                case 1: return INSTANCE;
                case 2: return JOIN;
                case 4: return SPECTATE;
                case 8: return JOIN_REQUEST;
                case 16: return SYNC;
                case 32: return PLAY;
                default: return EntityUtil.throwUnsupportedDiscordValue(value);
            }
        }
    }

    @Override
    public String toString() {
        return "Activity{" +
            "data=" + data +
            '}';
    }
}
