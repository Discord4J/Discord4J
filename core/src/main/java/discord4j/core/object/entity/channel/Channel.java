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

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Entity;
import discord4j.core.util.MentionUtil;
import discord4j.discordjson.json.ChannelData;
import discord4j.rest.entity.RestChannel;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.EnumSet;

/**
 * A Discord channel.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel">Channel Resource</a>
 */
public interface Channel extends Entity {

    /**
     * Gets the type of channel.
     *
     * @return The type of channel.
     */
    default Type getType() {
        return Type.of(getData().type());
    }

    @Override
    default Snowflake getId() {
        return Snowflake.of(getData().id());
    }

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
    default Mono<Void> delete(@Nullable String reason) {
        return getRestChannel().delete(reason);
    }

    /**
     * Gets the <i>raw</i> mention. This is the format utilized to directly mention another channel.
     *
     * @return The <i>raw</i> mention.
     */
    default String getMention() {
        return MentionUtil.forChannel(getId());
    }

    /**
     * Return a {@link RestChannel} handle to execute REST API operations on this entity.
     */
    RestChannel getRestChannel();

    /**
     * Gets the raw data as represented by Discord.
     *
     * @return The raw data as represented by Discord.
     */
    ChannelData getData();

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
        GUILD_STORE(6),

        GUILD_NEWS_THREAD(10),

        GUILD_PUBLIC_THREAD(11),

        GUILD_PRIVATE_THREAD(12),

        /** Represents a {@link StageChannel} for hosting events with an audience. */
        GUILD_STAGE_VOICE(13),

        /** Represents a {@link ForumChannel} that can only contain threads */
        GUILD_FORUM(15);

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
                case 10: return GUILD_NEWS_THREAD;
                case 11: return GUILD_PUBLIC_THREAD;
                case 12: return GUILD_PRIVATE_THREAD;
                case 13: return GUILD_STAGE_VOICE;
                case 15: return GUILD_FORUM;
                default: return UNKNOWN;
            }
        }
    }

    /** Represent channel flags : <a href="https://discord.com/developers/docs/resources/channel#channel-object-channel-flags">https://discord.com/developers/docs/resources/channel#channel-object-channel-flags</a> */
    @Experimental
    enum Flag {
        /**
         * This {@link ThreadChannel} is pinned to the top of its parent {@link ForumChannel}
         */
        PINNED(1),

        /**
         * Whether a tag is required to be specified when creating a {@link ThreadChannel} in a {@link ForumChannel}. Tags are specified in the applied_tags field.
         */
        REQUIRE_TAG(4);

        private final int shiftValue;
        private final int bitValue;

        Flag(int shiftValue) {
            this.shiftValue = shiftValue;
            this.bitValue = 1 << shiftValue;
        }

        /**
         * Gets the shift amount associated to this bit value
         *
         * @return N in 1 << N that is the bit value for this flag
         */
        public int getShiftValue() {
            return shiftValue;
        }

        /**
         * Gets the bit value associated to this flag
         *
         * @return The bit field value associated to this flag
         */
        public int getBitValue() {
            return bitValue;
        }

        /**
         * Translate a bitfield value into an {@link EnumSet < ForumChannelFlag >} related to known flags
         *
         * @param bitfield An integer representing the flags, one per bit
         * @return An {@link EnumSet<Flag>} of known flags associated to this bit field
         * @implNote This implementation ignores unknown flags
         */
        public static EnumSet<Flag> valueOf(final int bitfield) {
            EnumSet<Flag> returnSet = EnumSet.noneOf(Flag.class);
            for (Flag flag : Flag.values()) {
                if ((bitfield & flag.getBitValue()) != 0) {
                    returnSet.add(flag);
                }
            }
            return returnSet;
        }

        /**
         * Translates an {@link EnumSet< Flag >} to a binary bitfield
         *
         * @param flags Set of known forum channel flags
         * @return An integer representing the given set as an integer
         */
        public static int toBitfield(EnumSet<Flag> flags) {
            int bitfield = 0;
            for (Flag flag : flags) {
                bitfield |= flag.getBitValue();
            }
            return bitfield;
        }

    }
}
