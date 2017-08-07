/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.handle.audit;

import java.util.Arrays;

/**
 * The types of actions in an audit log.
 */
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
	MESSAGE_DELETE(72),

	UNKNOWN(-1);

	private final int raw;

	ActionType(int raw) {
		this.raw = raw;
	}

	/**
	 * Gets the value that Discord uses to represent the action type.
	 * @return The value that Discord uses to represent the action type.
	 */
	public int getRaw() {
		return raw;
	}

	/**
	 * Gets the action type represented by the given raw action type.
	 *
	 * @param raw The value that Discord uses to represent the action type.
	 * @return The action type represented by the given raw action type.
	 */
	public static ActionType fromRaw(int raw) {
		return Arrays.stream(values()).filter(a -> a.raw == raw).findFirst().orElse(UNKNOWN);
	}
}
