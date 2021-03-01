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
package discord4j.core.object.audit;

import java.util.Arrays;

/**
 * Represents the various type of audit log action.
 * See <a href="https://discord.com/developers/docs/resources/audit-log#audit-log-entry-object-audit-log-events">
 *     Audit Log Events</a>
 */
public enum ActionType {

    UNKNOWN(-1),
    GUILD_UPDATE(1),
    CHANNEL_CREATE(10),
    CHANNEL_UPDATE(11),
    CHANNEL_DELETE(12),
    CHANNEL_OVERWRITE_CREATE(13),
    CHANNEL_OVERWRITE_UPDATE(14),
    CHANNEL_OVERWRITE_DELETE(15),
    MEMBER_KICK(20),
    MEMBER_PRUNE(21),
    MEMBER_BAN_ADD(22),
    MEMBER_BAN_REMOVE(23),
    MEMBER_UPDATE(24),
    MEMBER_ROLE_UPDATE(25),
    MEMBER_MOVE(26),
    MEMBER_DISCONNECT(27),
    BOT_ADD(28),
    ROLE_CREATE(30),
    ROLE_UPDATE(31),
    ROLE_DELETE(32),
    INVITE_CREATE(40),
    INVITE_UPDATE(41),
    INVITE_DELETE(42),
    WEBHOOK_CREATE(50),
    WEBHOOK_UPDATE(51),
    WEBHOOK_DELETE(52),
    EMOJI_CREATE(60),
    EMOJI_UPDATE(61),
    EMOJI_DELETE(62),
    MESSAGE_DELETE(72),
    MESSAGE_BULK_DELETE(73),
    MESSAGE_PIN(74),
    MESSAGE_UNPIN(75),
    INTEGRATION_CREATE(80),
    INTEGRATION_UPDATE(81),
    INTEGRATION_DELETE(82);

    /**
     * Gets the type of action. It is guaranteed that invoking {@link #getValue()} from the returned enum will equal
     * ({@link #equals(Object)}) the supplied {@code value}.
     *
     * @param value The underlying value as represented by Discord.
     * @return The type of action.
     */
    public static ActionType of(final int value) {
        return Arrays.stream(values()).filter(type -> type.getValue() == value).findFirst().orElse(UNKNOWN);
    }

    /** The underlying value as represented by Discord. */
    private final int value;

    /**
     * Constructs an {@code ActionType}.
     *
     * @param value The underlying value as represented by Discord.
     */
    ActionType(final int value) {
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
}
