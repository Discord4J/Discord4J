package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.StickerData;

import java.util.*;

public final class Sticker implements Entity {

    /**
     * The gateway associated to this object.
     */
    private final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final StickerData data;

    public Sticker(final GatewayDiscordClient gateway, final StickerData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the data of the sticker.
     *
     * @return The data of the sticker.
     */
    public StickerData getData() {
        return data;
    }

    /**
     * Gets the ID of the pack the sticker is from.
     *
     * @return The ID of the pack the sticker is from.
     */
    public Snowflake getPackId() {
        return Snowflake.of(data.packId());
    }

    /**
     * Gets the name of the sticker.
     *
     * @return The name of the sticker.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the description of the sticker.
     *
     * @return The description of the sticker;
     */
    public String getDescription() {
        return data.description();
    }

    /**
     * Gets the list of tags for the sticker.
     *
     * @return The list of tags for the sticker.
     */
    public List<String> getTags() {
        return data.tags().toOptional()
            .map(tags -> tags.split(", "))
            .map(Arrays::asList)
            .orElse(Collections.emptyList());
    }

    /**
     * Gets the type of sticker format.
     *
     * @return The type of sticker format.
     */
    public Format getFormatType() {
        return Format.of(data.formatType());
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * The format of a sticker.
     *
     * @see <a href="TODO">Sticker Formats</a>
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

        private static Format of(final int value) {
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
}
