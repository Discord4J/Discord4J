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
package discord4j.core.object.presence;

/**
 * Represents the various type of status.
 * See <a href="https://discord.com/developers/docs/topics/gateway#update-status-status-types">
 *     Status Types</a>
 */
public enum Status {

    UNKNOWN("UNKNOWN"),
    ONLINE("online"),
    DO_NOT_DISTURB("dnd"),
    IDLE("idle"),
    INVISIBLE("invisible"),
    OFFLINE("offline");

    /** The underlying value as represented by Discord. */
    private final String value;

    /**
     * Constructs a {@code Status}.
     *
     * @param value The underlying value as represented by Discord.
     */
    Status(String value) {
        this.value = value;
    }

    /**
     * Gets the underlying value as represented by Discord.
     *
     * @return The underlying value as represented by Discord.
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the type of status. It is guaranteed that invoking {@link #getValue()} from the returned enum will equal
     * ({@link #equals(Object)}) the supplied {@code value}.
     *
     * @param value The underlying value as represented by Discord.
     * @return The type of status.
     */
    public static Status of(final String value) {
        switch (value) {
            case "idle": return IDLE;
            case "dnd": return DO_NOT_DISTURB;
            case "online": return ONLINE;
            case "invisible": return INVISIBLE;
            case "offline": return OFFLINE;
            default: return UNKNOWN;
        }
    }

    public enum Platform {
        DESKTOP,
        MOBILE,
        WEB
    }
}
