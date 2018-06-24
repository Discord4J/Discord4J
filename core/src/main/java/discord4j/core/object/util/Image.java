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

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents an image from Discord; specifically its URL.
 *
 * @see <a href="https://discordapp.com/developers/docs/reference#image-formatting">Image Formatting</a>
 */
public final class Image {

    /** The base (beginning) String for all {@link #getUrl() URLs}. */
    private static final String BASE_URL = "https://cdn.discordapp.com/";

    /** The {@link String#format(String, Object...) format} for URLs. */
    private static final String URL_FORMAT = BASE_URL + "%s%s";

    /**
     * Constructs an {@code Image} utilizing a path and the {@link Format} the URL should represent.
     *
     * @param path The path of the image; excluding the extension. Must be non-null.
     * @param format The {@link Format} the URL should represent. Must be non-null.
     * @return A constructed {@code Image} with an URL and the {@link Format} the URL represents.
     */
    public static Image of(final String path, final Format format) {
        return new Image(String.format(URL_FORMAT, path, format.extension), format);
    }

    /**
     * Constructs several {@code Image} instances utilizing a path and all the supported {@link Format formats}.
     *
     * @param path The path of the image; excluding the format. Must be non-null.
     * @param formats The {@link Format formats} supported by the URL. Must be non-null.
     * @return Several {@code Image} instances utilizing an URL and all the supported {@link Format formats}.
     */
    public static Set<Image> of(final String path, final Format...formats) {
        return Arrays.stream(formats).map(format -> of(path, format)).collect(Collectors.toSet());
    }

    /** The URL of the image. */
    private final String url;

    /** The {@link Format} the URL represents. */
    private final Format format;

    /**
     * Constructs a {@code Image} utilizing an URl and the {@link Format} the URL represents.
     *
     * @param url The URL of the image. Must be non-null.
     * @param format The {@link Format} the URL represents. Must be non-null.
     */
    private Image(final String url, final Format format) {
        this.url = Objects.requireNonNull(url);
        this.format = Objects.requireNonNull(format);
    }

    /**
     * Gets the URL of the image.
     *
     * @return The URL of the image.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the {@link Format} the URL represents.
     *
     * @return The {@link Format} the URL represents.
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Indicates whether some other object is "equal to" this {@code Image}.
     * The other object is considered equal if:
     * <ul>
     * <li>It is also a {@code Image} and;</li>
     * <li>Both instances have equal {@link #getUrl() urls}.</li>
     * </ul>
     *
     * @param obj An object to be tested for equality.
     * @return {@code true} if the other object is "equal to" this one, false otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof Image) && Objects.equals(url, ((Image) obj).url);
    }

    /**
     * Gets the hash code value of the {@link #getUrl() url}.
     *
     * @return The hash code value of the {@link #getUrl() url}.
     */
    @Override
    public int hashCode() {
        return url.hashCode();
    }

    /**
     * Gets the String represents of this {@code Image}.
     * <p>
     * The format returned by this method is unspecified and may vary between implementations; however, it is guaranteed
     * to always be non-empty. This method is not suitable for obtaining the URL; use {@link #getUrl()} instead.
     *
     * @return The String representation of this {@code Image}.
     * @see #getUrl()
     */
    @Override
    public String toString() {
        return "Image(" +
                "url='" + url + '\'' +
                ", format=" + format +
                ')';
    }

    /**
     * The format of an image; usually associated with an URL. This enum represents all the types supported by Discord.
     *
     * @see <a href="https://discordapp.com/developers/docs/reference#image-formatting-image-formats">Image Formats</a>
     */
    public enum Format {

        /** Represents the Joint Photographic Experts Group format. */
        JPEG(".jpeg"),

        /** Represents the Portable Network Graphics format. */
        PNG(".png"),

        /** Represents the WebP format. */
        WEB_P(".webp"),

        /** Represents the Graphics Interchange Format format. */
        GIF(".gif");

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
    }
}
