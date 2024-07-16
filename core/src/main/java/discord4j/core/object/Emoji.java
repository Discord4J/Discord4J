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

import discord4j.core.object.entity.GuildEmoji;
import discord4j.core.object.reaction.Reaction;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.ReactionData;
import discord4j.common.util.Snowflake;
import reactor.util.annotation.Nullable;

import java.util.Arrays;

/**
 * An emoji used for several components like {@link Reaction message reactions}, provides factory methods such as {@link #unicode(String)}
 * and others to generate reactions.
 */
public abstract class Emoji extends ReactionEmoji {

    /**
     * Constructs a {@code Emoji} using information from a {@link GuildEmoji known guild emoji}.
     *
     * @param emoji The guild emoji from which to take information.
     * @return An emoji using information from the given guild emoji.
     */
    public static Custom custom(GuildEmoji emoji) {
        return ReactionEmoji.custom(emoji);
    }

    /**
     * Constructs a {@code Emoji} for a custom emoji using the given information.
     *
     * @param id The ID of the custom emoji.
     * @param name The name of the custom emoji.
     * @param isAnimated Whether the custom emoji is animated.
     * @return An emoji using the given information.
     */
    public static Custom custom(Snowflake id, String name, boolean isAnimated) {
        return ReactionEmoji.custom(id, name, isAnimated);
    }

    /**
     * Constructs a {@code Emoji} for a unicode emoji.
     * <p>
     * The string argument to this method should be the exact UTF-16 encoded string of the desired emoji. For example,
     * <pre>
     * Emoji.unicode("&#92;u2764") // "heart"
     * Emoji.unicode("&#92;uD83D&#92;uDE00") // "grinning face"
     * Emoji.unicode("&#92;uD83D&#92;uDC68&#92;u200D&#92;uD83E&#92;uDDB0") // "man: red hair"
     * </pre>
     * A full list of emoji can be found <a href="https://unicode.org/emoji/charts/full-emoji-list.html">here</a>.
     * <p>
     * This method does <i>not</i> accept the "U+" notation for codepoints. For that, use
     * {@link #codepoints(String...)}.
     *
     * @param raw The raw unicode string for the emoji.
     * @return An emoji using the given information.
     */
    public static Unicode unicode(String raw) {
        return ReactionEmoji.unicode(raw);
    }

    /**
     * Constructs a {@code Emoji} for a unicode emoji.
     * <p>
     * The argument(s) to this method should use the "U+" notation for codepoints. For example,
     * <pre>
     * Emoji.codepoints("U+2764") // "heart"
     * Emoji.codepoints("U+1F600") // "grinning face"
     * Emoji.codepoints("U+1F468", "U+200D", "U+1F9B0") // "man: red hair"
     * </pre>
     * A full list of emoji can be found <a href="https://unicode.org/emoji/charts/full-emoji-list.html">here</a>.
     *
     * @param codepoints The codepoints that make up the emoji.
     * @return An emoji using the given information.
     */
    public static Unicode codepoints(String... codepoints) {
        String combined = Arrays.stream(codepoints)
            .map(c -> Integer.parseInt(c.substring(2), 16))
            .reduce(new StringBuilder(), StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();

        return unicode(combined);
    }

    /**
     * Constructs a {@code Emoji} for generic emoji information.
     *
     * @param id The ID of the custom emoji OR null if the emoji is a unicode emoji.
     * @param name The name of the custom emoji OR the raw unicode string for the emoji.
     * @param isAnimated Whether the emoji is animated OR false if the emoji is a unicode emoji.
     * @return An emoji using the given information.
     */
    public static Emoji of(@Nullable Long id, String name, boolean isAnimated) {
        return id == null ? unicode(name) : custom(Snowflake.of(id), name, isAnimated);
    }

    /**
     * Constructs a {@code Emoji} from a {@link ReactionData} representation.
     *
     * @param data the {@link ReactionData} wrapper.
     * @return An emoji using the given information.
     */
    public static Emoji of(ReactionData data) {
        return of(data.emoji());
    }

    /**
     * Constructs a {@code Emoji} from a {@link EmojiData} representation.
     *
     * @param data the {@link EmojiData} wrapper.
     * @return An emoji using the given information.
     */
    public static Emoji of(EmojiData data) {
        if (data.id().isPresent()) {
            return custom(Snowflake.of(data.id().get()),
                data.name().orElseThrow(IllegalArgumentException::new),
                data.animated().toOptional().orElse(false));
        }
        return unicode(data.name().orElseThrow(IllegalArgumentException::new));
    }

}
