/*
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.util;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;

import java.util.EnumSet;
import java.util.List;

/**
 * Handy permission checking utilities.
 */
public class PermissionsUtils {

	/**
	 * Checks a set of permissions provided by a guild against required permissions and a user's role hierarchy
	 * position.
	 *
	 * @param user     The user.
	 * @param guild    The guild.
	 * @param roles    The roles.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IUser user, IGuild guild, List<IRole> roles, EnumSet<Permissions> required)
			throws MissingPermissionsException {
		try {
			if (!isUserHigher(guild, user, roles))
				throw new MissingPermissionsException("Edited roles hierarchy is too high.");

			checkPermissions(user, guild, required);
		} catch (UnsupportedOperationException e) {
		}
	}

	/**
	 * Checks a set of permissions provided by a guild against required permissions and a user's role hierarchy
	 * position.
	 *
	 * @param user     The user.
	 * @param channel  The channel.
	 * @param roles    The roles.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IUser user, IChannel channel, List<IRole> roles, EnumSet<Permissions>
			required) throws MissingPermissionsException {
		try {
			if (!isUserHigher(channel.getGuild(), user, roles))
				throw new MissingPermissionsException("Edited roles hierarchy is too high.");

			checkPermissions(user, channel, required);
		} catch (UnsupportedOperationException e) {
		}
	}

	/**
	 * Checks a set of permissions provided by a channel against required permissions.
	 *
	 * @param user     The user.
	 * @param channel  The channel.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IUser user, IChannel channel, EnumSet<Permissions> required) throws
			MissingPermissionsException {
		try {
			EnumSet<Permissions> contained = channel.getModifiedPermissions(user);
			checkPermissions(contained, required);
		} catch (UnsupportedOperationException e) {
		}
	}

	/**
	 * Checks a set of permissions provided by a guild against required permissions.
	 *
	 * @param user     The user.
	 * @param guild    The guild.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IUser user, IGuild guild, EnumSet<Permissions> required) throws
			MissingPermissionsException {
		try {
			EnumSet<Permissions> contained = EnumSet.noneOf(Permissions.class);
			List<IRole> roles = user.getRolesForGuild(guild);
			for (IRole role : roles) {
				contained.addAll(role.getPermissions());
			}
			checkPermissions(contained, required);
		} catch (UnsupportedOperationException e) {
		}
	}

	/**
	 * Checks a set of permissions provided by a guild against required permissions and a user's role hierarchy
	 * position.
	 *
	 * @param client   The client.
	 * @param guild    The guild.
	 * @param roles    The roles.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IDiscordClient client, IGuild guild, List<IRole> roles,
										EnumSet<Permissions> required) throws MissingPermissionsException {
		checkPermissions(client.getOurUser(), guild, roles, required);
	}

	/**
	 * Checks a set of permissions provided by a guild against required permissions and a user's role hierarchy
	 * position.
	 *
	 * @param client   The client.
	 * @param channel  The channel.
	 * @param roles    The roles.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IDiscordClient client, IChannel channel, List<IRole> roles,
										EnumSet<Permissions> required) throws MissingPermissionsException {
		checkPermissions(client.getOurUser(), channel, roles, required);
	}

	/**
	 * Checks a set of permissions provided by a channel against required permissions.
	 *
	 * @param client   The client.
	 * @param channel  The channel.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IDiscordClient client, IChannel channel, EnumSet<Permissions> required) throws
			MissingPermissionsException {
		checkPermissions(client.getOurUser(), channel, required);
	}

	/**
	 * Checks a set of permissions provided by a guild against required permissions.
	 *
	 * @param client   The client.
	 * @param guild    The guild.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IDiscordClient client, IGuild guild, EnumSet<Permissions> required) throws
			MissingPermissionsException {
		checkPermissions(client.getOurUser(), guild, required);
	}

	/**
	 * Checks a set of permissions against required permissions.
	 *
	 * @param contained The permissions contained.
	 * @param required  The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(EnumSet<Permissions> contained, EnumSet<Permissions> required) throws
			MissingPermissionsException {
		if (contained.contains(Permissions.ADMINISTRATOR))
			return;

		EnumSet<Permissions> missing = EnumSet.noneOf(Permissions.class);

		for (Permissions requiredPermission : required) {
			if (!contained.contains(requiredPermission))
				missing.add(requiredPermission);
		}
		if (missing.size() > 0)
			throw new MissingPermissionsException(missing);
	}

	/**
	 * This checks if user1 can interact with the set of provided roles by checking their role hierarchies.
	 *
	 * @param guild The guild to check from.
	 * @param user1 The first user to check.
	 * @param roles The roles to check.
	 * @return True if user1's role hierarchy position > provided roles hierarchy.
	 */
	public static boolean isUserHigher(IGuild guild, IUser user1, List<IRole> roles) {
		List<IRole> user1Roles = guild.getRolesForUser(user1);
		int user1Position = 0;
		int rolesPosition = 0;
		for (IRole role : user1Roles)
			if (user1Position < role.getPosition())
				user1Position = role.getPosition();

		for (IRole role : roles)
			if (rolesPosition < role.getPosition())
				rolesPosition = role.getPosition();

		return user1Position > rolesPosition;
	}
}
