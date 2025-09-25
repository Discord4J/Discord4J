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
package discord4j.core.util;

import discord4j.rest.util.Image;

/** An utility class for image processing. */
public final class ImageUtil {

    /** The base (beginning) String for all {@link #getUrl(String, Image.Format)} URLs}. */
    private static final String BASE_URL = "https://cdn.discordapp.com/";

    /** The {@link String#format(String, Object...) format} for URLs. */
    private static final String URL_FORMAT = BASE_URL + "%s.%s";

    /**
     * Gets the URL utilizing a path and the {@link discord4j.rest.util.Image.Format} the URL should represent.
     *
     * @param path The path of the image; excluding the extension. Must be non-null.
     * @param format The {@link discord4j.rest.util.Image.Format} the URL should represent. Must be non-null.
     * @return The URL utilizing the path and the {@link discord4j.rest.util.Image.Format} the URL represents.
     */
    public static String getUrl(final String path, final Image.Format format) {
        return String.format(URL_FORMAT, path, format.getExtension());
    }

    private ImageUtil() {}
}
