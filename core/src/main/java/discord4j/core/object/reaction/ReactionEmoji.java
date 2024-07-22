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
package discord4j.core.object.reaction;

import discord4j.core.object.entity.Emoji;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.ReactionData;
import discord4j.common.util.Snowflake;
import reactor.util.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * An emoji used for {@link Reaction message reactions}, provides factory methods such as {@link #unicode(String)}
 * and others to generate reactions.
 */
public abstract class ReactionEmoji {

    /**
     * Constructs a {@code ReactionEmoji} using information from a {@link GuildEmoji known guild emoji}.
     *
     * @param emoji The guild emoji from which to take information.
     * @return A reaction emoji using information from the given guild emoji.
     */
    public static Custom custom(GuildEmoji emoji) {
        return new Custom(emoji.getId().asLong(), emoji.getName(), emoji.isAnimated());
    }

    /**
     * Constructs a {@code ReactionEmoji} using information from a {@link Emoji known emoji}.
     *
     * @param emoji The emoji from which to take information.
     * @return A reaction emoji using information from the given guild emoji.
     */
    public static Custom custom(Emoji emoji) {
        return new Custom(emoji.getId().asLong(), emoji.getName(), emoji.isAnimated());
    }

    /**
     * Constructs a {@code ReactionEmoji} for a custom emoji using the given information.
     *
     * @param id The ID of the custom emoji.
     * @param name The name of the custom emoji.
     * @param isAnimated Whether the custom emoji is animated.
     * @return A reaction emoji using the given information.
     */
    public static Custom custom(Snowflake id, @Nullable String name, boolean isAnimated) {
        return new Custom(id.asLong(), name, isAnimated);
    }

    /**
     * Constructs a {@code ReactionEmoji} for a unicode emoji.
     * <p>
     * The string argument to this method should be the exact UTF-16 encoded string of the desired emoji. For example,
     * <pre>
     * ReactionEmoji.unicode("&#92;u2764") // "heart"
     * ReactionEmoji.unicode("&#92;uD83D&#92;uDE00") // "grinning face"
     * ReactionEmoji.unicode("&#92;uD83D&#92;uDC68&#92;u200D&#92;uD83E&#92;uDDB0") // "man: red hair"
     * </pre>
     * A full list of emoji can be found <a href="https://unicode.org/emoji/charts/full-emoji-list.html">here</a>.
     * <p>
     * This method does <i>not</i> accept the "U+" notation for codepoints. For that, use
     * {@link #codepoints(String...)}.
     *
     * @param raw The raw unicode string for the emoji.
     * @return A reaction emoji using the given information.
     */
    public static Unicode unicode(String raw) {
        return new Unicode(raw);
    }

    /**
     * Constructs a {@code ReactionEmoji} for a unicode emoji.
     * <p>
     * The argument(s) to this method should use the "U+" notation for codepoints. For example,
     * <pre>
     * ReactionEmoji.codepoints("U+2764") // "heart"
     * ReactionEmoji.codepoints("U+1F600") // "grinning face"
     * ReactionEmoji.codepoints("U+1F468", "U+200D", "U+1F9B0") // "man: red hair"
     * </pre>
     * A full list of emoji can be found <a href="https://unicode.org/emoji/charts/full-emoji-list.html">here</a>.
     *
     * @param codepoints The codepoints that make up the emoji.
     * @return A reaction emoji using the given information.
     */
    public static Unicode codepoints(String... codepoints) {
        String combined = Arrays.stream(codepoints)
                .map(c -> Integer.parseInt(c.substring(2), 16))
                .reduce(new StringBuilder(), StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return unicode(combined);
    }

    /**
     * Constructs a {@code ReactionEmoji} for generic emoji information.
     *
     * @param id The ID of the custom emoji OR null if the emoji is a unicode emoji.
     * @param name The name of the custom emoji OR the raw unicode string for the emoji.
     * @param isAnimated Whether the emoji is animated OR false if the emoji is a unicode emoji.
     * @return A reaction emoji using the given information.
     */
    public static ReactionEmoji of(@Nullable Long id, String name, boolean isAnimated) {
        return id == null ? unicode(name) : custom(Snowflake.of(id), name, isAnimated);
    }

    /**
     * Constructs a {@code ReactionEmoji} from a {@link ReactionData} representation.
     *
     * @param data the {@link ReactionData} wrapper.
     * @return a reaction emoji using the given information.
     */
    public static ReactionEmoji of(ReactionData data) {
        return of(data.emoji());
    }

    /**
     * Constructs a {@code ReactionEmoji} from a {@link EmojiData} representation.
     *
     * @param data the {@link EmojiData} wrapper.
     * @return a reaction emoji using the given information.
     */
    public static ReactionEmoji of(EmojiData data) {
        if (data.id().isPresent()) {
            return custom(Snowflake.of(data.id().get()),
                    data.name().orElse(null),
                    data.animated().toOptional().orElse(false));
        }
        return unicode(data.name().orElseThrow(IllegalArgumentException::new));
    }

    /**
     * Gets the formatted version of this emoji (i.e., to display in the client).
     *
     * @return The formatted version of this emoji (i.e., to display in the client).
     */
    public abstract String asFormat();

    /**
     * Gets this emoji as downcasted to {@link Custom a custom reaction emoji}.
     *
     * @return This emoji downcasted to a custom emoji, if possible.
     */
    public Optional<Custom> asCustomEmoji() {
        return this instanceof Custom ? Optional.of((Custom) this) : Optional.empty();
    }

    /**
     * Gets this emoji downcasted to {@link Unicode a unicode reaction emoji}.
     *
     * @return This emoji downcasted to a unicode emoji, if possible.
     */
    public Optional<Unicode> asUnicodeEmoji() {
        return this instanceof Unicode ? Optional.of((Unicode) this) : Optional.empty();
    }

    /**
     * Converts this {@code ReactionEmoji} to a {@link EmojiData}.
     *
     * @return An {@link EmojiData} for this emoji.
     */
    public abstract EmojiData asEmojiData();

    public static final class Custom extends ReactionEmoji {

        private final long id;
        @Nullable
        private final String name;
        private final boolean isAnimated;

        private Custom(long id, @Nullable String name, boolean isAnimated) {
            this.id = id;
            this.name = name;
            this.isAnimated = isAnimated;
        }

        /**
         * Gets the id of the emoji.
         *
         * @return The id of the emoji.
         */
        public Snowflake getId() {
            return Snowflake.of(id);
        }

        /**
         * Gets the name of the emoji.
         * <br>
         * <b>Note:</b> this can be empty for reactions or onboarding.
         *
         * @return The name of the emoji.
         */
        public String getName() {
            return (name != null) ? name : "";
        }

        /**
         * Gets whether this emoji is animated.
         *
         * @return Whether this emoji is animated.
         */
        public boolean isAnimated() {
            return isAnimated;
        }

        @Override
        public EmojiData asEmojiData() {
            return EmojiData.builder()
                    .id(id)
                    .name(Optional.ofNullable(name))
                    .animated(isAnimated)
                    .build();
        }

        @Override
        public String asFormat() {
            return asFormat(this.isAnimated(), this.getName(), this.getId());
        }

        /**
         * Gets the formatted version of this emoji (i.e., to display in the client).
         * <br>
         * <b>Note:</b> please check first if {@link #getName()} is not empty.
         *
         * @param isAnimated Whether the emoji is animated.
         * @param id The ID of the custom emoji.
         * @param name The name of the custom emoji.
         * @return The formatted version of this emoji (i.e., to display in the client).
         */
        public static String asFormat(final boolean isAnimated, final String name, final Snowflake id) {
            return '<' + (isAnimated ? "a" : "") + ':' + Objects.requireNonNull(name) + ':' + Objects.requireNonNull(id).asString() + '>';
        }

        @Override
        public String toString() {
            return "ReactionEmoji.Custom{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", isAnimated=" + isAnimated +
                    "} " + super.toString();
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Custom custom = (Custom) o;
            return id == custom.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    public static final class Unicode extends ReactionEmoji {

        private final String raw;

        private Unicode(String raw) {
            this.raw = raw;
        }

        public String getRaw() {
            return raw;
        }

        @Override
        public EmojiData asEmojiData() {
            return EmojiData.builder()
                    .name(raw)
                    .build();
        }

        @Override
        public String asFormat() {
            return this.getRaw();
        }

        @Override
        public String toString() {
            return "ReactionEmoji.Unicode{" +
                    "raw='" + raw + '\'' +
                    "} " + super.toString();
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Unicode unicode = (Unicode) o;
            return Objects.equals(raw, unicode.raw);
        }

        @Override
        public int hashCode() {
            return raw.hashCode();
        }
    }

}
