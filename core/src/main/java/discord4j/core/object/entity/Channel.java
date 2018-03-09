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
package discord4j.core.object.entity;

/**
 * A Discord channel.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel">Channel Resource</a>
 */
public interface Channel extends Entity {

    /**
     * Gets the type of channel.
     *
     * @return The type of channel.
     */
    Type getType();

    /** Represents the various types of channels. */
    enum Type {

        /** Represents a {@link TextChannel}. */
        GUILD_TEXT(0),

        /** Represents a {@link PrivateChannel}. */
        DM(1),

        /** Represents a {@link VoiceChannel}. */
        GUILD_VOICE(2),

        /** Represents a group DM. */
        GROUP_DM(3),

        /** Represents a {@link Category}. */
        GUILD_CATEGORY(4);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code Channel.Type}.
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
