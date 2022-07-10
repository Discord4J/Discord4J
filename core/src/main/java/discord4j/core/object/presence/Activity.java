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

import discord4j.common.util.Snowflake;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.ActivityData;
import discord4j.discordjson.json.ActivityUpdateRequest;
import discord4j.discordjson.possible.Possible;

import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;

public class Activity {

    private final ActivityData data;

    Activity(final ActivityData data) {
        this.data = data;
    }

    /**
     * Gets the data of the activity.
     *
     * @return The data of the activity.
     */
    public ActivityData getData() {
        return data;
    }

    /**
     * Gets the type for this activity.
     *
     * @return The type for this activity.
     */
    public Type getType() {
        return Type.of(data.type());
    }

    /**
     * Gets the activity's name.
     *
     * @return The activity's name.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the stream URL, if present.
     *
     * @return The stream url, if present.
     */
    public Optional<String> getStreamingUrl() {
        return Possible.flatOpt(data.url());
    }

    /**
     * Gets unix timestamp of when the activity was added to the user's session.
     *
     * @return The unix timestamp of when the activity was added to the user's session.
     */
    public Instant getCreatedAt() {
        return Instant.ofEpochMilli(data.createdAt());
    }

    /**
     * Gets the UNIX time (in milliseconds) of when the activity started, if present.
     *
     * @return The UNIX time (in milliseconds) of when the activity started, if present.
     */
    public Optional<Instant> getStart() {
        return data.timestamps().toOptional()
                .flatMap(timestamps -> timestamps.start().toOptional())
                .map(Instant::ofEpochMilli);
    }

    /**
     * Gets the UNIX time (in milliseconds) of when the activity ends, if present.
     *
     * @return The UNIX time (in milliseconds) of when the activity ends, if present.
     */
    public Optional<Instant> getEnd() {
        return data.timestamps().toOptional()
                .flatMap(timestamps -> timestamps.end().toOptional())
                .map(Instant::ofEpochMilli);
    }

    /**
     * Gets the application ID for the game, if present.
     *
     * @return The application ID for the game, if present.
     */
    public Optional<Snowflake> getApplicationId() {
        return data.applicationId().toOptional()
                .map(Snowflake::of);
    }

    /**
     * Gets what the player is currently doing, if present.
     *
     * @return What the player is currently doing, if present.
     */
    public Optional<String> getDetails() {
        return Possible.flatOpt(data.details());
    }

    /**
     * Gets the user's current party status, if present.
     *
     * @return The user's current party status, if present.
     */
    public Optional<String> getState() {
        return Possible.flatOpt(data.state());
    }

    /**
     * Gets the ID of the party, if present.
     *
     * @return The ID of the party, if present.
     */
    public Optional<String> getPartyId() {
        return data.party().toOptional()
                .flatMap(party -> party.id().toOptional());
    }

    /**
     * Gets the party's current size, if present.
     *
     * @return The party's current size, if present.
     */
    public OptionalLong getCurrentPartySize() {
        return data.party().toOptional()
                .flatMap(party -> party.size().toOptional())
                .map(size -> size.get(0))
                .map(OptionalLong::of)
                .orElseGet(OptionalLong::empty);
    }

    /**
     * Gets the party's max size, if present.
     *
     * @return The party's max size, if present.
     */
    public OptionalLong getMaxPartySize() {
        return data.party().toOptional()
                .flatMap(party -> party.size().toOptional())
                .map(size -> size.get(1))
                .map(OptionalLong::of)
                .orElseGet(OptionalLong::empty);
    }

    /**
     * Gets the ID for a large asset of the activity, usually a {@code Snowflake}, if present.
     *
     * @return The ID for a large asset of the activity, usually a {@code Snowflake}, if present.
     */
    public Optional<String> getLargeImageId() {
        return data.assets().toOptional()
                .flatMap(assets -> assets.largeImage().toOptional());
    }

    /**
     * Gets the text displayed when hovering over the large image of the activity, if present.
     *
     * @return The text displayed when hovering over the large image of the activity, if present.
     */
    public Optional<String> getLargeText() {
        return data.assets().toOptional()
                .flatMap(assets -> assets.largeText().toOptional());
    }

    /**
     * Gets the ID for a small asset of the activity, usually a {@code Snowflake}, if present.
     *
     * @return The ID for a small asset of the activity, usually a {@code Snowflake}, if present.
     */
    public Optional<String> getSmallImageId() {
        return data.assets().toOptional()
                .flatMap(assets -> assets.smallImage().toOptional());
    }

    /**
     * Gets the text displayed when hovering over the small image of the activity, if present.
     *
     * @return The text displayed when hovering over the small image of the activity, if present.
     */
    public Optional<String> getSmallText() {
        return data.assets().toOptional()
                .flatMap(assets -> assets.smallText().toOptional());
    }

    /**
     * Gets the secret for joining a party, if present.
     *
     * @return The secret for joining a party, if present.
     */
    public Optional<String> getJoinSecret() {
        return data.secrets().toOptional()
                .flatMap(secrets -> secrets.join().toOptional());
    }

    /**
     * Gets the secret for spectating a game, if present.
     *
     * @return The secret for spectating a game, if present.
     */
    public Optional<String> getSpectateSecret() {
        return data.secrets().toOptional()
                .flatMap(secrets -> secrets.spectate().toOptional());
    }

    /**
     * Gets the secret for a specific instanced match, if present.
     *
     * @return The secret for a specific instanced match, if present.
     */
    public Optional<String> getMatchSecret() {
        return data.secrets().toOptional()
                .flatMap(secrets -> secrets.match().toOptional());
    }

    /**
     * Gets the emoji used for a custom status, if present.
     *
     * @return The emoji used for a custom status, if present.
     */
    public Optional<ReactionEmoji> getEmoji() {
        return Possible.flatOpt(data.emoji())
                .map(emoji -> {
                    // TODO FIXME
                    String sid = emoji.id().toOptional().map(Id::asString).orElse(null);
                    Long id = sid == null ? null : Snowflake.asLong(sid);
                    return ReactionEmoji.of(id, emoji.name(),
                            emoji.animated().toOptional().orElse(false));
                });
    }

    /**
     * Gets whether or not the activity is an instanced game session.
     *
     * @return Whether or not the activity is an instanced game session.
     */
    public boolean isInstance() {
        return data.instance().toOptional().orElse(false);
    }

    public EnumSet<Flag> getFlags() {
        return data.flags().toOptional()
                .map(flags -> Arrays.stream(Flag.values())
                        .filter(f -> (flags & f.getValue()) == f.getValue())
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Flag.class))))
                .orElse(EnumSet.noneOf(Flag.class));
    }

    /** The type of "action" for an activity. */
    public enum Type {

        /** Unknown type. **/
        UNKNOWN(-1),

        /** "Playing {name}" */
        PLAYING(0),

        /** "Streaming {details}" */
        STREAMING(1),

        /** "Listening to {name}" */
        LISTENING(2),

        /** "Watching {name}" */
        WATCHING(3),

        /** {emoji} {name} */
        CUSTOM(4),

        /** "Competing in {name}" */
        COMPETING(5);

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
                case 4: return CUSTOM;
                case 5: return COMPETING;
                default: return UNKNOWN;
            }
        }
    }

    public enum Flag {

        UNKNOWN(0),

        INSTANCE(1 << 0),
        JOIN(1 << 1),
        SPECTATE(1 << 2),
        JOIN_REQUEST(1 << 3),
        SYNC(1 << 4),
        PLAY(1 << 5),
        PARTY_PRIVACY_FRIENDS(1 << 6),
        PARTY_PRIVACY_VOICE_CHANNEL(1 << 7),
        EMBEDDED(1 << 8),;

        private final int value;

        Flag(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Flag of(final int value) {
            switch (value) {
                case 1 << 0: return INSTANCE;
                case 1 << 1: return JOIN;
                case 1 << 2: return SPECTATE;
                case 1 << 3: return JOIN_REQUEST;
                case 1 << 4: return SYNC;
                case 1 << 5: return PLAY;
                case 1 << 6: return PARTY_PRIVACY_FRIENDS;
                case 1 << 7: return PARTY_PRIVACY_VOICE_CHANNEL;
                case 1 << 8: return EMBEDDED;
                default: return UNKNOWN;
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
