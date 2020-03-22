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
package discord4j.core.object.entity.channel;

import discord4j.core.object.entity.Entity;
import discord4j.rest.entity.RestChannel;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

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

    /**
     * Requests to delete this channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the channel has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    default Mono<Void> delete() {
        return delete(null);
    }

    /**
     * Requests to delete this channel while optionally specifying a reason.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the channel has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    Mono<Void> delete(@Nullable String reason);

    /**
     * Gets the <i>raw</i> mention. This is the format utilized to directly mention another channel.
     *
     * @return The <i>raw</i> mention.
     */
    default String getMention() {
        return "<#" + getId().asString() + '>';
    }

    /**
     * Return a {@link RestChannel} handle to execute REST API operations on this entity.
     */
    RestChannel getRestChannel();

    /** Represents the various types of channels. */
    enum Type {

        /** Unknown type. */
        UNKNOWN(-1),

        /** Represents a {@link TextChannel}. */
        GUILD_TEXT(0),

        /** Represents a {@link PrivateChannel}. */
        DM(1),

        /** Represents a {@link VoiceChannel}. */
        GUILD_VOICE(2),

        /** Represents a group DM. */
        GROUP_DM(3),

        /** Represents a {@link Category}. */
        GUILD_CATEGORY(4),

        /** Represents a {@link NewsChannel}. */
        GUILD_NEWS(5),

        /** Represents a {@link StoreChannel}. */
        GUILD_STORE(6);

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

        /**
         * Gets the type of channel. It is guaranteed that invoking {@link #getValue()} from the returned enum will
         * equal ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of channel.
         */
        public static Type of(final int value) {
            switch (value) {
                case 0: return GUILD_TEXT;
                case 1: return DM;
                case 2: return GUILD_VOICE;
                case 3: return GROUP_DM;
                case 4: return GUILD_CATEGORY;
                case 5: return GUILD_NEWS;
                case 6: return GUILD_STORE;
                default: return UNKNOWN;
            }
        }
    }
}
