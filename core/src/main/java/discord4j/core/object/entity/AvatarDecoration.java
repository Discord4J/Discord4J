package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.core.util.ImageUtil;
import discord4j.discordjson.json.AvatarDecorationData;

import java.util.Objects;

import static discord4j.rest.util.Image.Format.PNG;

/**
 * Represents the {@link User} avatar decoration.
 * @see <a href="https://support.discord.com/hc/en-us/articles/13410113109911">Avatar Decoration - Discord Support</a>
 * @see <a href="https://discord.com/developers/docs/resources/user#avatar-decoration-data-object">Avatar Decoration - Discord API</a>
 */
public class AvatarDecoration implements DiscordObject {

    /** The path for {@code AvatarDecoration} image URLs. */
    private static final String AVATAR_DECORATION_IMAGE_PATH = "avatar-decoration-presets/%s";

    /**
     * The gateway associated to this object.
     */
    protected final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final AvatarDecorationData data;

    public AvatarDecoration(final GatewayDiscordClient gateway, final AvatarDecorationData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    /**
     * Gets the id of the avatar decoration's SKU
     *
     * @return The id of the avatar decoration's SKU
     */
    public Snowflake getSkuId() {
        return Snowflake.of(this.data.skuId());
    }

    /**
     * Gets the avatar decoration hash.
     *
     * @return The hash of the avatar decoration.
     */
    public String getAsset() {
        return this.data.asset();
    }

    /**
     * Gets the URL for this Avatar Decoration.
     *
     * @return The URL for this avatar decoration.
     */
    public String getImageUrl() {
        final String path = String.format(AVATAR_DECORATION_IMAGE_PATH, this.getAsset());
        return ImageUtil.getUrl(path, PNG);
    }

    @Override
    public String toString() {
        return "AvatarDecoration{" +
            "data=" + data +
            '}';
    }

}
