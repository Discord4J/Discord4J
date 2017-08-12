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
 * The Discord permissions.
 */
public enum Permissions {

	/**
	 * Allows the creation of instant invites.
	 */
	CREATE_INVITE(0),
	/**
	 * Allows kicking members.
	 */
	KICK(1),
	/**
	 * Allows banning members.
	 */
	BAN(2),
	/**
	 * Allows all permissions and bypasses channel permission overrides.
	 */
	ADMINISTRATOR(3),
	/**
	 * Allows management and editing of channels.
	 */
	MANAGE_CHANNELS(4),
	/**
	 * Allows management and editing of a channel.
	 */
	MANAGE_CHANNEL(4),
	/**
	 * Allows management and editing of the guild.
	 */
	MANAGE_SERVER(5),
	/**
	 * Allows for the addition of reactions to messages.
	 */
	ADD_REACTIONS(6),
	/**
	 * Allows for viewing of audit logs.
	 */
	VIEW_AUDIT_LOG(7),
	/**
	 * Allows reading of messages in a channel.
	 */
	READ_MESSAGES(10),
	/**
	 * Allows for sending messages in a channel.
	 */
	SEND_MESSAGES(11),
	/**
	 * Allows for sending of text-to-speech messages.
	 */
	SEND_TTS_MESSAGES(12),
	/**
	 * Allows for deletion of other users messages.
	 */
	MANAGE_MESSAGES(13),
	/**
	 * Allows links sent by the user to be automatically embedded.
	 */
	EMBED_LINKS(14),
	/**
	 * Allows for uploading of images and files.
	 */
	ATTACH_FILES(15),
	/**
	 * Allows for reading of message history.
	 */
	READ_MESSAGE_HISTORY(16),
	/**
	 * Allows for using the @everyone tag to notify all users and the @here tag to notify all online users in a channel.
	 */
	MENTION_EVERYONE(17),
	/**
	 * Allows the usage of custom emojis from other servers.
	 */
	USE_EXTERNAL_EMOJIS(18),
	/**
	 * Allows for joining of a voice channel.
	 */
	VOICE_CONNECT(20),
	/**
	 * Allows for muting members in a voice channel.
	 */
	VOICE_SPEAK(21),
	/**
	 * Allows for muting members in a voice channel.
	 */
	VOICE_MUTE_MEMBERS(22),
	/**
	 * Allows for deafening of members in a voice channel.
	 */
	VOICE_DEAFEN_MEMBERS(23),
	/**
	 * Allows for moving of members between voice channels.
	 */
	VOICE_MOVE_MEMBERS(24),
	/**
	 * Allows for using voice-activity-detection in a voice channel.
	 */
	VOICE_USE_VAD(25),
	/**
	 * Allows for modification of a user's own nickname.
	 */
	CHANGE_NICKNAME(26),
	/**
	 * Allows for modification of other users nicknames.
	 */
	MANAGE_NICKNAMES(27),
	/**
	 * Allows for management and editing of roles.
	 */
	MANAGE_ROLES(28),
	/**
	 * Allows for management and editing of permissions.
	 */
	MANAGE_PERMISSIONS(28),
	/**
	 * Allows for management and editing of webhooks.
	 */
	MANAGE_WEBHOOKS(29),
	/**
	 * Allows for management and editing of emojis.
	 */
	MANAGE_EMOJIS(30);

	/**
	 * The bit offset for the permission.
	 */
	private final int offset;

	Permissions(int offset) {
		this.offset = offset;
	}

	/**
	 * Gets whether the given "permissions number" contains the permission.
	 *
	 * @param permissionsNumber The raw permissions number.
	 * @param checkAdmin Whether the check should take into account the permissionsNumber having administrator permission.
	 * @return Whether the given permissions number contains the permission.
	 */
	public boolean hasPermission(int permissionsNumber, boolean checkAdmin) {
		if ((1 << offset & permissionsNumber) > 0)
			return true;
		else if (!this.equals(ADMINISTRATOR) && checkAdmin)
			return ADMINISTRATOR.hasPermission(permissionsNumber);
		return false;
	}

	/**
	 * Gets whether the given "permissions number" contains the permission.
	 *
	 * <p>Equivalent to <code>hasPermission(number, true)</code>
	 *
	 * @param permissionsNumber The raw permissions number.
	 * @return Whether the given permissions number contains the permission.
	 */
	public boolean hasPermission(int permissionsNumber) {
		return hasPermission(permissionsNumber, true);
	}

	/**
	 * Gets a set of allowed permissions represented by the given raw permissions number.
	 *
	 * @param permissionsNumber The raw permissions number.
	 * @return A set of allowed permissions represented by the given raw permissions number.
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
	 * Gets a set of denied permissions represented by the given raw permissions number.
	 *
	 * @param permissionsNumber The raw permissions number.
	 * @return A set of denied permissions represented by the given raw permissions number.
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
	 * Gets the raw permissions number for the given set of permissions.
	 *
	 * @param permissions The permissions.
	 * @return The raw permissions number for the given set of permissions.
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
