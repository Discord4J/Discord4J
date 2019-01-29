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

import discord4j.core.internal.util.EntityUtil;

public enum ActionType {

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
    MESSAGE_DELETE(72);

    public static ActionType of(int value) {
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
            default: return EntityUtil.throwUnsupportedDiscordValue(value);
        }
    }

    private final int value;

    ActionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
