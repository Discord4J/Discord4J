package discord4j.core.object.emoji;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.EmojiData;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * A Discord emoji.
 * <p>
 * <a href="https://discord.com/developers/docs/resources/emoji#emoji-resource">Emoji Resource</a>
 */
public class EmojiCustom extends Emoji {

    /** The path for {@code Emoji} image URLs. */
    static final String EMOJI_IMAGE_PATH = "emojis/%s";

    /** The raw data as represented by Discord. */
    final EmojiData data;

    public EmojiCustom(EmojiData data) {
        this.data = Objects.requireNonNull(data);
    }

    EmojiCustom(long id, @Nullable String name, boolean isAnimated) {
        this.data = EmojiData.builder().id(id).name(Optional.ofNullable(name)).animated(isAnimated).build();
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
     * Gets the id of the emoji.
     *
     * @return The id of the emoji.
     */
    public Snowflake getId() {
        return data.id()
            .map(Snowflake::of)
            .orElseThrow(IllegalStateException::new); // this should be safe for emojis
    }

    /**
     * Gets the name of the emoji.
     * <br>
     * <b>Note:</b> this can be empty for reactions or onboarding.
     *
     * @return The name of the emoji.
     */
    public String getName() {
        return data.name()
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

    @Override
    public EmojiData asEmojiData() {
        return EmojiData.builder().from(this.data).build();
    }

    @Override
    public String asFormat() {
        return asFormat(this.isAnimated(), this.getName(), this.getId());
    }

    /**
     * Gets the formatted version of this emoji (i.e., to display in the client).
     * <br>
     * <b>Note:</b> please check first if {@link #getName()} is not empty.
     *
     * @param isAnimated Whether the emoji is animated.
     * @param id The ID of the custom emoji.
     * @param name The name of the custom emoji.
     * @return The formatted version of this emoji (i.e., to display in the client).
     */
    public static String asFormat(final boolean isAnimated, final String name, final Snowflake id) {
        return '<' + (isAnimated ? "a" : "") + ':' + Objects.requireNonNull(name) + ':' + Objects.requireNonNull(id).asString() + '>';
    }

    @Override
    public String toString() {
        return "CustomEmoji{" +
            "data=" + data +
            '}';
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmojiCustom custom = (EmojiCustom) o;
        return getId().asLong() == custom.getId().asLong();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId().asLong());
    }
}
