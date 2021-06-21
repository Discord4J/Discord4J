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
 * @see <a href="https://github.com/discord/discord-api-docs/blob/master/docs/Reference.md#message-formatting">Timestamp message formatting</a>
 */
public final class Timestamp {

    private Timestamp() {}

    /**
     * Gets the markdown representation of the provided {@code Instant} using the provided style.
     * In a message, Discord will display the timestamp in the user's local timezone.
     *
     * @param instant the {@code Instant} to construct a timestamp with.
     * @param style the style used to tell Discord how the timestamp should be formatted.
     * @return The markdown representation of the {@code Instant} as a String.
     */
    public static String of(Instant instant, String style) {
        return "<t:" + instant.getEpochSecond() + ":" + style + ">";
    }

    /**
     * Gets the markdown representation of the provided {@code Instant} with the default style (Short date/time).
     * In a message, Discord will display the timestamp in the user's local timezone.
     *
     * @param instant the {@code Instant} to construct a timestamp with.
     * @return The markdown representation of the {@code Instant} as a String.
     */
    public static String of(Instant instant) {
        return "<t:" + instant.getEpochSecond() + ">";
    }

    /**
     * Gets the markdown representation of the provided {@code Instant} using the style {@code t} (Short time).
     * In a message, Discord will display the timestamp in the user's local timezone.
     *
     * @param instant the {@code Instant} to construct a timestamp with.
     * @return The markdown representation of the {@code Instant} as a String.
     */
    public static String shortTime(Instant instant) {
        return of(instant, "t");
    }

    /**
     * Gets the markdown representation of the provided {@code Instant} using the style {@code T} (Long time).
     * In a message, Discord will display the timestamp in the user's local timezone.
     *
     * @param instant the {@code Instant} to construct a timestamp with.
     * @return The markdown representation of the {@code Instant} as a String.
     */
    public static String longTime(Instant instant) {
        return of(instant, "T");
    }

    /**
     * Gets the markdown representation of the provided {@code Instant} using the style {@code d} (Short date).
     * In a message, Discord will display the timestamp in the user's local timezone.
     *
     * @param instant the {@code Instant} to construct a timestamp with.
     * @return The markdown representation of the {@code Instant} as a String.
     */
    public static String shortDate(Instant instant) {
        return of(instant, "d");
    }

    /**
     * Gets the markdown representation of the provided {@code Instant} using the style {@code D} (Long date).
     * In a message, Discord will display the timestamp in the user's local timezone.
     *
     * @param instant the {@code Instant} to construct a timestamp with.
     * @return The markdown representation of the {@code Instant} as a String.
     */
    public static String longDate(Instant instant) {
        return of(instant, "D");
    }

    /**
     * Gets the markdown representation of the provided {@code Instant} using the default style {@code f} (Short date/time).
     * In a message, Discord will display the timestamp in the user's local timezone.
     *
     * @param instant the {@code Instant} to construct a timestamp with.
     * @return The markdown representation of the {@code Instant} as a String.
     */
    public static String shortDateTime(Instant instant) {
        return of(instant);
    }

    /**
     * Gets the markdown representation of the provided {@code Instant} using the style {@code F} (Long date/time).
     * In a message, Discord will display the timestamp in the user's local timezone.
     *
     * @param instant the {@code Instant} to construct a timestamp with.
     * @return The markdown representation of the {@code Instant} as a String.
     */
    public static String longDateTime(Instant instant) {
        return of(instant, "F");
    }

    /**
     * Gets the markdown representation of the provided {@code Instant} using the style {@code R} (Relative time).
     * In a message, Discord will display the timestamp in the user's local timezone.
     *
     * @param instant the {@code Instant} to construct a timestamp with.
     * @return The markdown representation of the {@code Instant} as a String.
     */
    public static String relativeTime(Instant instant) {
        return of(instant, "R");
    }
}
