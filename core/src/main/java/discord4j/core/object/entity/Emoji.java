package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.util.ImageUtil;
import discord4j.discordjson.json.EmojiData;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static discord4j.rest.util.Image.Format.GIF;
import static discord4j.rest.util.Image.Format.PNG;

/**
 * A Discord emoji.
 * <p>
 * <a href="https://discord.com/developers/docs/resources/emoji#emoji-resource">Emoji Resource</a>
 */
public abstract class Emoji implements Entity {

    /** The path for {@code Emoji} image URLs. */
    static final String EMOJI_IMAGE_PATH = "emojis/%s";

    /** The gateway associated to this object. */
    final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    final EmojiData data;

    /**
     * Constructs a {@code Emoji} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    protected Emoji(GatewayDiscordClient gateway, EmojiData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    public abstract Mono<User> getUser();

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return data.id()
            .map(Snowflake::of)
            .orElseThrow(IllegalStateException::new); // this should be safe for emojis
    }

    /**
     * Gets the data of the emoji.
     *
     * @return The data of the emoji.
     */
    public EmojiData getData() {
        return data;
    }

    /**
     * Gets the emoji name.
     *
     * @return The emoji name.
     */
    public String getName() {
        return data.name()
            .orElseThrow(IllegalStateException::new); // this should be safe for emojis
    }

    /**
     * Gets whether this emoji must be wrapped in colons.
     *
     * @return {@code true} if this emoji must be wrapped in colons, {@code false} otherwise.
     */
    public boolean requiresColons() {
        return data.requireColons().toOptional()
            .orElseThrow(IllegalStateException::new); // this should be safe for emojis
    }

    /**
     * Gets whether this emoji is managed.
     *
     * @return {@code true} if this emoji is managed, {@code false} otherwise.
     */
    public boolean isManaged() {
        return data.managed().toOptional()
            .orElseThrow(IllegalStateException::new); // this should be safe for emojis
    }

    /**
     * Gets whether this emoji is animated.
     *
     * @return {@code true} if this emoji is animated, {@code false} otherwise.
     */
    public boolean isAnimated() {
        return data.animated().toOptional()
            .orElseThrow(IllegalStateException::new); // this should be safe for emojis
    }

    /**
     * Gets whether this emoji is available for use.
     *
     * @return {@code true} if this emoji is available, {@code false} otherwise (due to loss of Server Boosts for
     * example).
     */
    public boolean isAvailable() {
        return data.available().toOptional()
            .orElseThrow(IllegalStateException::new); // this should be safe for emojis
    }

    /**
     * Gets the URL for this emoji.
     *
     * @return The URL for this emoji.
     */
    public String getImageUrl() {
        final String path = String.format(EMOJI_IMAGE_PATH, getId().asString());
        return isAnimated() ? ImageUtil.getUrl(path, GIF) : ImageUtil.getUrl(path, PNG);
    }

    /**
     * Gets the image for this emoji.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Image image} of the emoji. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Image> getImage() {
        return Image.ofUrl(getImageUrl());
    }

    /**
     * Gets the formatted version of this emoji (i.e., to display in the client).
     *
     * @return The formatted version of this emoji (i.e., to display in the client).
     */
    public String asFormat() {
        return ReactionEmoji.Custom.custom(this).asFormat();
    }

    @Override
    public String toString() {
        return "Emoji{" +
            "data=" + data +
            '}';
    }
}
