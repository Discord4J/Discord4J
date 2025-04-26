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

}
