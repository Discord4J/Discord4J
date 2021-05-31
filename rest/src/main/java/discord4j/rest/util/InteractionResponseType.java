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

package discord4j.rest.util;

public enum InteractionResponseType {
    /**
     * Unknown type
     */
    UNKNOWN(-1),
    /**
     * ACK a Ping
     */
    PONG(1),
    /**
     * Respond to an interaction with a message
     */
    CHANNEL_MESSAGE_WITH_SOURCE(4),
    /**
     * ACK an interaction and send a response later, the user sees a loading state
     */
    DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE(5);

    /**
     * The underlying value as represented by Discord.
     */
    private final int value;

    /**
     * Constructs an {@code InteractionResponseType}.
     *
     * @param value The underlying value as represented by Discord.
     */
    InteractionResponseType(final int value) {
        this.value = value;
    }

    /**
     * Gets the underlying value as represented by Discord.
     *
     * @return The underlying value as represented by Discord.
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the type of an interaction response. It is guaranteed that invoking {@link #getValue()} from the returned enum will
     * equal ({@code ==}) the supplied {@code value}.
     *
     * @param value The underlying value as represented by Discord.
     * @return The type of response.
     */
    public static InteractionResponseType of(final int value) {
        switch (value) {
            case 1: return PONG;
            case 4: return CHANNEL_MESSAGE_WITH_SOURCE;
            case 5: return DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE;
            default: return UNKNOWN;
        }
    }
}
