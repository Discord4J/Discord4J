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

import discord4j.core.object.entity.GuildEmoji;
import discord4j.core.object.util.Snowflake;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public abstract class ReactionEmoji {

    public static Custom custom(GuildEmoji emoji) {
        return new Custom(emoji.getId().asLong(), emoji.getName(), emoji.isAnimated());
    }

    public static Custom custom(Snowflake id, String name, boolean isAnimated) {
        return new Custom(id.asLong(), name, isAnimated);
    }

    public static Unicode unicode(String raw) {
        return new Unicode(raw);
    }

    public static ReactionEmoji of(@Nullable Long id, String name, boolean isAnimated) {
        return id == null ? unicode(name) : custom(Snowflake.of(id), name, isAnimated);
    }

    public Optional<Custom> asCustomEmoji() {
        return this instanceof Custom ? Optional.of((Custom) this) : Optional.empty();
    }

    public Optional<Unicode> asUnicodeEmoji() {
        return this instanceof Custom ? Optional.of((Unicode) this) : Optional.empty();
    }

    public static final class Custom extends ReactionEmoji {

        private final long id;
        private final String name;
        private final boolean isAnimated;

        private Custom(long id, String name, boolean isAnimated) {
            this.id = id;
            this.name = name;
            this.isAnimated = isAnimated;
        }

        public Snowflake getId() {
            return Snowflake.of(id);
        }

        public String getName() {
            return name;
        }

        public boolean isAnimated() {
            return isAnimated;
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
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Custom custom = (Custom) o;
            return id == custom.id &&
                    isAnimated == custom.isAnimated &&
                    Objects.equals(name, custom.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, isAnimated);
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
        public String toString() {
            return "ReactionEmoji.Unicode{" +
                    "raw='" + raw + '\'' +
                    "} " + super.toString();
        }

        @Override
        public boolean equals(Object o) {
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
