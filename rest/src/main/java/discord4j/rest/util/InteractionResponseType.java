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
    DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE(5),

    /**
     * For components, ACK an interaction and edit the original message later; the user does not see a loading state
     */
    DEFERRED_UPDATE_MESSAGE(6),

    /**
     * For components, edit the message the component was attached to
     */
    UPDATE_MESSAGE(7),

    /**
     * Respond to an autocomplete interaction with suggested choices
     */
    APPLICATION_COMMAND_AUTOCOMPLETE_RESULT(8),

    /**
     * Response to a supported interaction with a modal
     */
    MODAL(9),

    /**
     * Response to a supported interaction with a notification that this interaction is only available to premium guilds or users.
     * @deprecated in favor of using {@link discord4j.core.object.component.Button#premium(Snowflake)}. This will continue to function but may eventually be unsupported
     */
    @Deprecated
    PREMIUM_REQUIRED(10);

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
            case 6: return DEFERRED_UPDATE_MESSAGE;
            case 7: return UPDATE_MESSAGE;
            case 8: return APPLICATION_COMMAND_AUTOCOMPLETE_RESULT;
            case 9: return MODAL;
            case 10: return PREMIUM_REQUIRED;
            default: return UNKNOWN;
        }
    }
}
