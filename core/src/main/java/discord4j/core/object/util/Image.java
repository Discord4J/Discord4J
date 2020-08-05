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
package discord4j.core.object.util;

import discord4j.common.ReactorResources;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Base64;
import java.util.Objects;

/**
 * Represents a Discord image.
 *
 * @see <a href="https://discordapp.com/developers/docs/reference#image-formatting">Image Formatting</a>
 */
public final class Image {

    /**
     * Constructs an {@code Image} utilizing raw image data.
     *
     * @param image The raw image data.
     * @param format The {@link Format} of the data.
     * @return An {@code Image} with raw image data.
     */
    public static Image ofRaw(final byte[] image, final Format format) {
        return new Image(Base64.getEncoder().encodeToString(image), format);
    }

    /**
     * Constructs an {@code Image} using the resource at the given url.
     *
     * @param url The url of the image.
     * @return A {@link Mono} where, upon successful completion, emits an {@link Image} with the data at the url. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public static Mono<Image> ofUrl(final String url) {
        return ReactorResources.DEFAULT_HTTP_CLIENT.get()
                .get()
                .uri(url)
                .responseSingle((res, body) -> body.asByteArray().map(image -> {
                    Format format = Format.fromContentType(res.responseHeaders().get("Content-Type"));
                    return Image.ofRaw(image, format);
                }));
    }

    /** The hash of the image. */
    private final String hash;

    /** The format of the image. */
    private final Format format;

    private Image(final String hash, final Format format) {
        this.hash = Objects.requireNonNull(hash);
        this.format = Objects.requireNonNull(format);
    }

    /**
     * Gets the hash of the image.
     *
     * @return The hash of the image.
     */
    public String getHash() {
        return hash;
    }

    /**
     * Gets the format of the image.
     *
     * @return The format of the image.
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Gets a data URI for this image.
     *
     * @return The data URI for this image.
     */
    public String getData() {
        return String.format("data:image/%s;base64,%s", format.extension, hash);
    }

    /**
     * Indicates whether some other object is "equal to" this {@code Image}.
     * The other object is considered equal if:
     * <ul>
     * <li>It is also a {@code Image} and;</li>
     * <li>Both instances have equal {@link #getData()} data}.</li>
     * </ul>
     *
     * @param obj An object to be tested for equality.
     * @return {@code true} if the other object is "equal to" this one, false otherwise.
     */
    @Override
    public boolean equals(@Nullable final Object obj) {
        return (obj instanceof Image) && ((Image) obj).getData().equals(getData());
    }

    /**
     * Gets the hash code value of the {@link #getData()} data}.
     *
     * @return The hash code value of the {@link #getData()} data}.
     */
    @Override
    public int hashCode() {
        return getData().hashCode();
    }

    /**
     * Gets the String represents of this {@code Image}.
     * <p>
     * The format returned by this method is unspecified and may vary between implementations; however, it is guaranteed
     * to always be non-empty. This method is not suitable for obtaining the data; use {@link #getData()} instead.
     *
     * @return The String representation of this {@code Image}.
     * @see #getData()
     */
    @Override
    public String toString() {
        return "Image{" +
                "hash='" + hash + '\'' +
                ", format=" + format +
                '}';
    }

    /**
     * The format of an image. This enum represents all the types supported by Discord.
     *
     * @see <a href="https://discordapp.com/developers/docs/reference#image-formatting-image-formats">Image Formats</a>
     */
    public enum Format {

        /** Unknown image format */
        UNKNOWN("UNKNOWN"),

        /** Represents the Joint Photographic Experts Group format. */
        JPEG("jpeg"),

        /** Represents the Portable Network Graphics format. */
        PNG("png"),

        /** Represents the WebP format. */
        WEB_P("webp"),

        /** Represents the Graphics Interchange Format format. */
        GIF("gif");

        /** The file extension associated with this format. */
        private final String extension;

        /**
         * Constructs a {@code Image.Format}.
         *
         * @param extension The file extension associated with this format.
         */
        Format(final String extension) {
            this.extension = extension;
        }

        /**
         * Gets the file extension associated with this format.
         *
         * @return The file extension associated with this format.
         */
        public String getExtension() {
            return extension;
        }

        private static Format fromContentType(String contentType) {
            switch (contentType) {
                case "image/jpeg": return JPEG;
                case "image/png": return PNG;
                case "image/webp": return WEB_P;
                case "image/gif": return GIF;
                default: return UNKNOWN;
            }
        }
    }
}
