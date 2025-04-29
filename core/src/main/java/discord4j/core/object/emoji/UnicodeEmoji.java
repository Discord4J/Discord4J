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
package discord4j.core.object.emoji;

import discord4j.discordjson.json.EmojiData;
import reactor.util.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a Unicode emoji.
 * <br>
 * <b>Example:</b> 🔥
 */
public class UnicodeEmoji extends Emoji {

    private final String raw;

    UnicodeEmoji(String raw) {
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
        return "UnicodeEmoji{" +
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
        UnicodeEmoji unicode = (UnicodeEmoji) o;
        return Objects.equals(raw, unicode.getRaw());
    }

    @Override
    public int hashCode() {
        return raw.hashCode();
    }

    /**
     * Constructs a {@code UnicodeEmoji} for a raw emoji.
     * <p>
     * The string argument to this method should be the exact UTF-16 encoded string of the desired emoji. For example,
     * <pre>
     * UnicodeEmoji.of("&#92;u2764") // "heart"
     * UnicodeEmoji.of("&#92;uD83D&#92;uDE00") // "grinning face"
     * UnicodeEmoji.of("&#92;uD83D&#92;uDC68&#92;u200D&#92;uD83E&#92;uDDB0") // "man: red hair"
     * </pre>
     * A full list of emoji can be found <a href="https://unicode.org/emoji/charts/full-emoji-list.html">here</a>.
     * <p>
     * This method does <i>not</i> accept the "U+" notation for codepoints. For that, use
     * {@link #ofCodePoints(String...)}.
     *
     * @param raw The raw Unicode string for the emoji.
     * @return A Unicode emoji using the given information.
     */
    public static UnicodeEmoji of(String raw) {
        return new UnicodeEmoji(raw);
    }

    /**
     * Constructs a {@code UnicodeEmoji} for a Codepoint Unicode emoji.
     * <p>
     * The argument(s) to this method should use the "U+" notation for codepoints. For example,
     * <pre>
     * UnicodeEmoji.ofCodePoints("U+2764") // "heart"
     * UnicodeEmoji.ofCodePoints("U+1F600") // "grinning face"
     * UnicodeEmoji.ofCodePoints("U+1F468", "U+200D", "U+1F9B0") // "man: red hair"
     * </pre>
     * A full list of emoji can be found <a href="https://unicode.org/emoji/charts/full-emoji-list.html">here</a>.
     *
     * @param codepoints The codepoints that make up the emoji.
     * @return A reaction emoji using the given information.
     */
    public static UnicodeEmoji ofCodePoints(String... codepoints) {
        String combined = Arrays.stream(codepoints)
            .map(c -> Integer.parseInt(c.substring(2), 16))
            .reduce(new StringBuilder(), StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();

        return UnicodeEmoji.of(combined);
    }

}
