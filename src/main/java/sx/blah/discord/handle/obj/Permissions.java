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

package sx.blah.discord.handle.obj;

import java.util.EnumSet;

/**
 * This represents Discord permissions.
 */
public enum Permissions {

	/**
	 * Allows the user to create invites.
	 */
	CREATE_INVITE(0),
	/**
	 * Allows the user to kick users.
	 */
	KICK(1),
	/**
	 * Allows the user to ban users.
	 */
	BAN(2),
	/**
	 * Allows the user to manage roles.
	 * NOTE: This supercedes any other permissions if true.
	 */
	ADMINISTRATOR(3),
	/**
	 * Allows the user to manage channels.
	 */
	MANAGE_CHANNELS(4),
	/**
	 * Allows the user to manage a specific channel.
	 */
	MANAGE_CHANNEL(4),
	/**
	 * Allows the user to manage a server.
	 */
	MANAGE_SERVER(5),
	/**
	 * Allows the user to add emoji reactions.
	 */
	ADD_REACTIONS(6),
	/**
	 * Allows the user to read the audit log.
	 */
	VIEW_AUDIT_LOG(7),
	/**
	 * Allows the user to read messages.
	 */
	READ_MESSAGES(10),
	/**
	 * Allows the user to send messages.
	 */
	SEND_MESSAGES(11),
	/**
	 * Allows the user to send messages with tts.
	 */
	SEND_TTS_MESSAGES(12),
	/**
	 * Allows the user to manage messages.
	 */
	MANAGE_MESSAGES(13),
	/**
	 * Allows the user to embed links in messages.
	 */
	EMBED_LINKS(14),
	/**
	 * Allows the user to attach files in chat.
	 */
	ATTACH_FILES(15),
	/**
	 * Allows the user to read message history.
	 */
	READ_MESSAGE_HISTORY(16),
	/**
	 * Allows the user to @mention everyone.
	 */
	MENTION_EVERYONE(17),
	/**
	 * Allows the user to use emojis from external servers in a given one.
	 */
	USE_EXTERNAL_EMOJIS(18),
	/**
	 * Allows the user to connect to a voice channel.
	 */
	VOICE_CONNECT(20),
	/**
	 * Allows the user to speak in a voice channel.
	 */
	VOICE_SPEAK(21),
	/**
	 * Allows the user to globally mute users in a voice channel.
	 */
	VOICE_MUTE_MEMBERS(22),
	/**
	 * Allows the user to globally deafen users in a voice channel.
	 */
	VOICE_DEAFEN_MEMBERS(23),
	/**
	 * Allows the user to move users to different voice channels.
	 */
	VOICE_MOVE_MEMBERS(24),
	/**
	 * Allows the user to use "voice activation detection".
	 */
	VOICE_USE_VAD(25),
	/**
	 * Allows the user to change his/her own nicknames.
	 */
	CHANGE_NICKNAME(26),
	/**
	 * Allows the user to manage nicknames.
	 */
	MANAGE_NICKNAMES(27),
	/**
	 * Allows the user to manage roles below yours.
	 */
	MANAGE_ROLES(28),
	/**
	 * Allows the user to manage permissions.
	 */
	MANAGE_PERMISSIONS(28),
	/**
	 * Allows the user to manage webhooks.
	 */
	MANAGE_WEBHOOKS(29),
	/**
	 * Allows the user to manage server emojis.
	 */
	MANAGE_EMOJIS(30);

	/**
	 * The bit offset in the permissions number
	 */
	private final int offset;

	Permissions(int offset) {
		this.offset = offset;
	}

	/**
	 * Checks whether a provided "permissions number" contains this permission.
	 *
	 * @param permissionsNumber The raw permissions number.
	 * @param checkAdmin Set to true in order to have the ADMINISTRATOR permission supersede the provided permissions.
	 * @return True if the user has this permission, false if otherwise.
	 */
	public boolean hasPermission(int permissionsNumber, boolean checkAdmin) {
		if ((1 << offset & permissionsNumber) > 0)
			return true;
		else if (!this.equals(ADMINISTRATOR) && checkAdmin)
			return ADMINISTRATOR.hasPermission(permissionsNumber);
		return false;
	}

	/**
	 * Checks whether a provided "permissions number" contains this permission. Same as calling #hasPermission(number, true).
	 *
	 * @param permissionsNumber The raw permissions number.
	 * @return True if the user has this permission, false if otherwise.
	 */
	public boolean hasPermission(int permissionsNumber) {
		return hasPermission(permissionsNumber, true);
	}

	/**
	 * Generates a set of allowed permissions represented by the given raw permissions number.
	 *
	 * @param permissionsNumber The raw permissions number.
	 * @return The set of permissions represented by the number.
	 */
	public static EnumSet<Permissions> getAllowedPermissionsForNumber(int permissionsNumber) {
		EnumSet<Permissions> permissionsSet = EnumSet.noneOf(Permissions.class);

		for (Permissions permission : EnumSet.allOf(Permissions.class)) {
			if (permission.hasPermission(permissionsNumber))
				permissionsSet.add(permission);
		}

		return permissionsSet;
	}

	/**
	 * Generates a set of denied permissions represented by the given raw permissions number.
	 *
	 * @param permissionsNumber The raw permissions number.
	 * @return The set of permissions represented by the number.
	 */
	public static EnumSet<Permissions> getDeniedPermissionsForNumber(int permissionsNumber) {
		EnumSet<Permissions> permissionsSet = EnumSet.noneOf(Permissions.class);

		for (Permissions permission : EnumSet.allOf(Permissions.class)) {
			if (permission.hasPermission(permissionsNumber, false))
				permissionsSet.add(permission);
		}

		return permissionsSet;
	}

	/**
	 * Generates a raw permissions number for the provided set of permissions.
	 *
	 * @param permissions The permissions.
	 * @return The raw permissions number.
	 */
	public static int generatePermissionsNumber(EnumSet<Permissions> permissions) {
		if (permissions == null)
			permissions = EnumSet.noneOf(Permissions.class);

		int number = 0;
		for (Permissions permission : permissions) {
			number |= (1 << permission.offset);
		}
		return number;
	}
}
