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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.StickerData;

import java.util.*;

public class Sticker extends PartialSticker {

    public Sticker(final GatewayDiscordClient gateway, final StickerData data) {
        super(gateway, data);
    }

    /**
     * Gets the data of the sticker.
     *
     * @return The data of the sticker.
     */
    public StickerData getData() {
        return (StickerData) super.getStickerData();
    }

    /**
     * Gets the ID of the pack the sticker is from, if present.
     *
     * @return The ID of the pack the sticker is from, if present.
     */
    public Optional<Snowflake> getPackId() {
        return getData().packId().toOptional().map(Snowflake::of);
    }

    /**
     * Gets the description of the sticker, if present.
     *
     * @return The description of the sticker, if present.
     */
    public Optional<String> getDescription() {
        return getData().description();
    }

    /**
     * Gets the list of tags for the sticker.
     *
     * @return The list of tags for the sticker.
     */
    public /*~~>*/List<String> getTags() {
        return getData().tags().toOptional()
            .map(tags -> tags.split(", "))
            .map(Arrays::asList)
            .orElse(Collections.emptyList());
    }

    /**
     * Gets whether this sticker is available for use.
     *
     * @return {@code true} if this sticker is available, {@code false} otherwise (due to loss of Server Boosts for example).
     */
    public boolean isAvailable() {
        return getData().available().toOptional()
            .orElseThrow(IllegalStateException::new); // this should be safe
    }

    /**
     * Gets the type of sticker.
     *
     * @return The type of sticker.
     */
    public Type getType() {
        return Type.of(getData().type());
    }

    /**
     * The format of a sticker.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#message-object-message-sticker-format-types">
     *     Sticker Formats</a>
     */
    public enum Format {

        /**
         * Unknown sticker format.
         */
        UNKNOWN(0),

        /**
         * Represents the Portable Network Graphics format.
         */
        PNG(1),

        /**
         * Represents the Animated Portable Network Graphics format.
         */
        APNG(2),

        /**
         * Represents the Lottie format.
         */
        LOTTIE(3);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * Constructs a {@code Sticker.Format}.
         */
        Format(final int value) {
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

        public static Format of(final int value) {
            switch (value) {
                case 1:
                    return PNG;
                case 2:
                    return APNG;
                case 3:
                    return LOTTIE;
                default:
                    return UNKNOWN;
            }
        }
    }

    /**
     * The type of sticker.
     *
     * @see <a href="https://discord.com/developers/docs/resources/sticker#sticker-object-sticker-types">
     *     Sticker Types</a>
     */
    public enum Type {
        /**
         * Unknown sticker type.
         */
        UNKNOWN(0),

        /**
         * Represents an official sticker in a pack, part of Nitro or in a removed purchasable pack.
         */
        STANDARD(1),

        /**
         * Represents a sticker uploaded to a Boosted guild for the guild's members.
         */
        GUILD(2);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * Constructs a {@code Sticker.Type}.
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

        public static Type of(final int value) {
            switch (value) {
                case 1:
                    return STANDARD;
                case 2:
                    return GUILD;
                default:
                    return UNKNOWN;
            }
        }
    }
}
