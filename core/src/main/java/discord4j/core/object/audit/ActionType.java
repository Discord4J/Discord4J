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
    INTEGRATION_DELETE(82),
    GUILD_SCHEDULED_EVENT_CREATE(100),
    GUILD_SCHEDULED_EVENT_UPDATE(101),
    GUILD_SCHEDULED_EVENT_DELETE(102),
    THREAD_CREATE(110),
    THREAD_UPDATE(111),
    THREAD_DELETE(112),
    APPLICATION_COMMAND_PERMISSION_UPDATE(121),
    AUTO_MODERATION_RULE_CREATE(140),
    AUTO_MODERATION_RULE_UPDATE(141),
    AUTO_MODERATION_RULE_DELETE(142),
    AUTO_MODERATION_BLOCK_MESSAGE(143),
    AUTO_MODERATION_FLAG_TO_CHANNEL(144),
    AUTO_MODERATION_USER_COMMUNICATION_DISABLED(145),
    CREATOR_MONETIZATION_REQUEST_CREATED(150),
    CREATOR_MONETIZATION_TERMS_ACCEPTED(151);


    /**
     * Gets the type of action. It is guaranteed that invoking {@link #getValue()} from the returned enum will equal
     * ({@link #equals(Object)}) the supplied {@code value}.
     *
     * @param value The underlying value as represented by Discord.
     * @return The type of action.
     */
    public static ActionType of(final int value) {
        switch (value) {
            case 1: return GUILD_UPDATE;
            case 10: return CHANNEL_CREATE;
            case 11: return CHANNEL_UPDATE;
            case 12: return CHANNEL_DELETE;
            case 13: return CHANNEL_OVERWRITE_CREATE;
            case 14: return CHANNEL_OVERWRITE_UPDATE;
            case 15: return CHANNEL_OVERWRITE_DELETE;
            case 20: return MEMBER_KICK;
            case 21: return MEMBER_PRUNE;
            case 22: return MEMBER_BAN_ADD;
            case 23: return MEMBER_BAN_REMOVE;
            case 24: return MEMBER_UPDATE;
            case 25: return MEMBER_ROLE_UPDATE;
            case 26: return MEMBER_MOVE;
            case 27: return MEMBER_DISCONNECT;
            case 28: return BOT_ADD;
            case 30: return ROLE_CREATE;
            case 31: return ROLE_UPDATE;
            case 32: return ROLE_DELETE;
            case 40: return INVITE_CREATE;
            case 41: return INVITE_UPDATE;
            case 42: return INVITE_DELETE;
            case 50: return WEBHOOK_CREATE;
            case 51: return WEBHOOK_UPDATE;
            case 52: return WEBHOOK_DELETE;
            case 60: return EMOJI_CREATE;
            case 61: return EMOJI_UPDATE;
            case 62: return EMOJI_DELETE;
            case 72: return MESSAGE_DELETE;
            case 73: return MESSAGE_BULK_DELETE;
            case 74: return MESSAGE_PIN;
            case 75: return MESSAGE_UNPIN;
            case 80: return INTEGRATION_CREATE;
            case 81: return INTEGRATION_UPDATE;
            case 82: return INTEGRATION_DELETE;
            case 100: return GUILD_SCHEDULED_EVENT_CREATE;
            case 101: return GUILD_SCHEDULED_EVENT_UPDATE;
            case 102: return GUILD_SCHEDULED_EVENT_DELETE;
            case 110: return THREAD_CREATE;
            case 111: return THREAD_UPDATE;
            case 112: return THREAD_DELETE;
            case 121: return APPLICATION_COMMAND_PERMISSION_UPDATE;
            case 140: return AUTO_MODERATION_RULE_CREATE;
            case 141: return AUTO_MODERATION_RULE_UPDATE;
            case 142: return AUTO_MODERATION_RULE_DELETE;
            case 143: return AUTO_MODERATION_BLOCK_MESSAGE;
            case 144: return AUTO_MODERATION_FLAG_TO_CHANNEL;
            case 145: return AUTO_MODERATION_USER_COMMUNICATION_DISABLED;
            case 150: return CREATOR_MONETIZATION_REQUEST_CREATED;
            case 151: return CREATOR_MONETIZATION_TERMS_ACCEPTED;
            default: return UNKNOWN;
        }
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
