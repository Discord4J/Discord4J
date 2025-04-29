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
package discord4j.core.object.emoji;

import discord4j.common.util.Snowflake;
import discord4j.core.util.ImageUtil;
import discord4j.discordjson.json.EmojiData;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;

import static discord4j.rest.util.Image.Format.GIF;
import static discord4j.rest.util.Image.Format.PNG;

/**
 * A Discord emoji.
 * <p>
 * <a href="https://discord.com/developers/docs/resources/emoji#emoji-resource">Emoji Resource</a>
 */
public class CustomEmoji extends Emoji {

    /** The path for {@code Emoji} image URLs. */
    static final String EMOJI_IMAGE_PATH = "emojis/%s";

    /** The raw data as represented by Discord. */
    final EmojiData data;

    protected CustomEmoji(EmojiData data) {
        this.data = Objects.requireNonNull(data);
    }

    CustomEmoji(long id, @Nullable String name, boolean isAnimated) {
        this.data = EmojiData.builder()
            .id(id)
            .name(Optional.ofNullable(name))
            .animated(isAnimated)
            .build();
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
     * Gets whether this emoji must be wrapped in colons.
     *
     * @return {@code true} if this emoji must be wrapped in colons, {@code false} otherwise.
     */
    public boolean requiresColons() {
        return data.requireColons().toOptional()
            .orElse(true); // this should be safe for emojis
    }

    /**
     * Gets whether this emoji is managed.
     *
     * @return {@code true} if this emoji is managed, {@code false} otherwise.
     */
    public boolean isManaged() {
        return data.managed().toOptional()
            .orElse(false);
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

    @Override
    public EmojiData asEmojiData() {
        return EmojiData.builder().from(this.data).build();
    }

    @Override
    public String asFormat() {
        return asFormat(this.isAnimated(), this.getName(), this.getId());
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
        CustomEmoji custom = (CustomEmoji) o;
        return getId().asLong() == custom.getId().asLong();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId().asLong());
    }

    /**
     * Constructs a {@code CustomEmoji} using the given information.
     *
     * @param id The ID of the custom emoji.
     * @param name The name of the custom emoji.
     * @param isAnimated Whether the custom emoji is animated.
     * @return A custom emoji using the given information.
     */
    public static CustomEmoji of(Snowflake id, @Nullable String name, boolean isAnimated) {
        return new CustomEmoji(id.asLong(), name, isAnimated);
    }

    /**
     * Constructs a {@code CustomEmoji} from a {@link EmojiData} representation.
     *
     * @param data the {@link EmojiData} wrapper.
     * @return a custom emoji using the given information.
     */
    public static CustomEmoji of(EmojiData data) {
        return new CustomEmoji(data);
    }
}
