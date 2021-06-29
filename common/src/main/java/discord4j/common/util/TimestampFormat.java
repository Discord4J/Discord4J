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
package discord4j.common.util;

import java.time.Instant;

/**
 * A utility class for formatting a Java {@link Instant} as a timestamp in Discord messages.
 *
 * @see <a href="https://discord.com/developers/docs/reference#message-formatting-timestamp-styles">Timestamp message formatting</a>
 */
public enum TimestampFormat {
    /**
     * Example: {@code 20 April 2021 16:20}
     */
    DEFAULT(""),
    /**
     * Example: {@code 16:20}
     */
    SHORT_TIME("t"),
    /**
     * Example: {@code 	16:20:30}
     */
    LONG_TIME("T"),
    /**
     * Example: {@code 	20/04/2021}
     */
    SHORT_DATE("d"),
    /**
     * Example: {@code 	20 April 2021}
     */
    LONG_DATE("D"),
    /**
     * Example: {@code 	20 April 2021 16:20}
     */
    SHORT_DATE_TIME("f"),
    /**
     * Example: {@code Tuesday, 20 April 2021 16:20}
     */
    LONG_DATE_TIME("F"),
    /**
     * Example: {@code 	2 months ago}
     */
    RELATIVE_TIME("R");

    private final String style;

    TimestampFormat(String style) {
        this.style = style;
    }

    /**
     * Get the style (1 character) for this timestamp format. The default format returns an empty string.
     * @return The style character as a {@code String}
     */
    public String getStyle() {
        return style;
    }

    /**
     * Gets the markdown representation of the provided {@code Instant} for this style.
     * In a message, Discord will display the timestamp in the user's local timezone.
     *
     * @param instant the {@code Instant} to construct a timestamp with.
     * @return The markdown representation of the {@code Instant} as a String.
     */
    public String format(Instant instant) {
        if (style.isEmpty())
            return "<t:" + instant.getEpochSecond() + ">";
        else
            return "<t:" + instant.getEpochSecond() + ":" + style + ">";
    }
}
