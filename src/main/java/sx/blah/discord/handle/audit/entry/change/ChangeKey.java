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

package sx.blah.discord.handle.audit.entry.change;

import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.PermissionOverride;
import sx.blah.discord.util.LogMarkers;

/**
 * The keys used in an {@link ChangeMap}. Use these with
 * {@link sx.blah.discord.handle.audit.entry.AuditLogEntry#getChangeByKey(ChangeKey)}.
 *
 * @param <T> The type of the value associated with the key.
 */
public final class ChangeKey<T> {
	public static final ChangeKey<String> NAME = newKey();
	public static final ChangeKey<String> ICON_HASH = newKey();
	public static final ChangeKey<String> SPLASH_HASH = newKey();
	public static final ChangeKey<Long> OWNER_ID = newKey();
	public static final ChangeKey<String> REGION = newKey();
	public static final ChangeKey<Long> AFK_CHANNEL_ID = newKey();
	public static final ChangeKey<Integer> AFK_TIMEOUT = newKey();
	public static final ChangeKey<Integer> MFA_LEVEL = newKey();
	public static final ChangeKey<Integer> VERIFICATION_LEVEL = newKey();
	public static final ChangeKey<Integer> EXPLICIT_CONTENT_FILTER = newKey();
	public static final ChangeKey<Integer> DEFAULT_MESSAGE_NOTIFICATIONS = newKey();
	public static final ChangeKey<String> VANITY_URL_CODE = newKey();
	public static final ChangeKey<IRole[]> $ADD = newKey();
	public static final ChangeKey<IRole[]> $REMOVE = newKey();
	public static final ChangeKey<Integer> PRUNE_DELETE_DAYS = newKey();
	public static final ChangeKey<Boolean> WIDGET_ENABLED = newKey();
	public static final ChangeKey<Long> WIDGET_CHANNEL_ID = newKey();
	public static final ChangeKey<Integer> POSITION = newKey();
	public static final ChangeKey<String> TOPIC = newKey();
	public static final ChangeKey<Integer> BITRATE = newKey();
	public static final ChangeKey<Boolean> NSFW = newKey();
	public static final ChangeKey<Long> APPLICATION_ID = newKey();
	public static final ChangeKey<Integer> PERMISSIONS = newKey();
	public static final ChangeKey<Integer> COLOR = newKey();
	public static final ChangeKey<Boolean> HOIST = newKey();
	public static final ChangeKey<Boolean> MENTIONABLE = newKey();
	public static final ChangeKey<Integer> ALLOW = newKey();
	public static final ChangeKey<Integer> DENY = newKey();
	public static final ChangeKey<String> CODE = newKey();
	public static final ChangeKey<Long> CHANNEL_ID = newKey();
	public static final ChangeKey<Long> INVITER_ID = newKey();
	public static final ChangeKey<Integer> MAX_USES = newKey();
	public static final ChangeKey<Integer> USES = newKey();
	public static final ChangeKey<Integer> MAX_AGE = newKey();
	public static final ChangeKey<Boolean> TEMPORARY = newKey();
	public static final ChangeKey<Boolean> DEAF = newKey();
	public static final ChangeKey<Boolean> MUTE = newKey();
	public static final ChangeKey<String> NICK = newKey();
	public static final ChangeKey<String> AVATAR_HASH = newKey();
	public static final ChangeKey<Long> ID = newKey();
	public static final ChangeKey<Object> TYPE = newKey();
	public static final ChangeKey<PermissionOverride[]> PERMISSION_OVERWRITES = newKey();

	public static ChangeKey<?> fromRaw(String rawKey) {
		ChangeKey<?> key = null;
		try {
			key = (ChangeKey) ChangeKey.class.getDeclaredField(rawKey.toUpperCase()).get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Failed to find change key from raw string: {}", rawKey, e);
		}
		return key;
	}

	private static <T> ChangeKey<T> newKey() {
		return new ChangeKey<>();
	}

	private ChangeKey() {

	}
}
