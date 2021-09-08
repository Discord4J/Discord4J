/*
 *  This file is part of Discord4J.
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

package discord4j.rest.util;

/**
 * @see <a href="https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-types">
 * Application Command Type</a>
 */
public enum ApplicationCommandType {
    CHAT_INPUT(1),
    USER(2),
    MESSAGE(3),
    STRING(3);

    /**
     * The underlying value as represented by Discord.
     */
    private final int value;

    /**
     * Constructs an {@code ApplicationCommandType}.
     *
     * @param value The underlying value as represented by Discord.
     */
    ApplicationCommandType(final int value) {
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
     * Gets the type of an application command. It is guaranteed that invoking {@link #getValue()} from the
     * returned enum will
     * equal ({@code ==}) the supplied {@code value}.
     *
     * @param value The underlying value as represented by Discord.
     * @return The type of command.
     */
    public static ApplicationCommandType of(final int value) {
        switch (value) {
            case 2: return USER;
            case 3: return MESSAGE;
            default: return CHAT_INPUT;
        }
    }
}
