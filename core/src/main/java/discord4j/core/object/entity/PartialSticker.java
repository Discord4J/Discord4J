package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.util.ImageUtil;
import discord4j.discordjson.json.PartialStickerData;

import java.util.Objects;

import static discord4j.rest.util.Image.Format.LOTTIE;
import static discord4j.rest.util.Image.Format.PNG;

public class PartialSticker implements Entity {

    /** The path for {@code PartialSticker} image URLs. */
    private static final String STICKER_IMAGE_PATH = "stickers/%s";

    /**
     * The gateway associated to this object.
     */
    protected final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final PartialStickerData data;

    public PartialSticker(final GatewayDiscordClient gateway, final PartialStickerData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
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
     * Gets the type of sticker format.
     *
     * @return The type of sticker format.
     */
    public Sticker.Format getFormatType() {
        return Sticker.Format.of(data.formatType());
    }

    /**
     * Gets the URL for this sticker.
     *
     * @return The URL for this sticker.
     */
    public String getImageUrl() {
        final String path = String.format(STICKER_IMAGE_PATH, getId().asString());
        return getFormatType().equals(Sticker.Format.LOTTIE) ? ImageUtil.getUrl(path, LOTTIE) : ImageUtil.getUrl(path, PNG);
    }

    /**
     * Gets the data of the sticker.
     *
     * @return The data of the sticker.
     */
    public PartialStickerData getStickerData() {
        return data;
    }
}
