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

package sx.blah.discord.util;

import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.cache.Cache;

import java.util.*;

/**
 * Utility for permission checking.
 */
public class PermissionUtils {
	/**
	 * Throws a {@link MissingPermissionsException} if user1 is not higher in the role hierarchy than user2 in the given guild.
	 * This is determined by the positions of each of the users' highest roles.
	 *
	 * @param guild The guild both users belongs to.
	 * @param user1 The user who must be higher in the role hierarchy.
	 * @param user2 The user who must be lower in the role hierarchy.
	 */
	public static void requireUserHigher(IGuild guild, IUser user1, IUser user2) {
		if (!isUserHigher(guild, user1, user2))
			throw new MissingPermissionsException("Attempt to interact with user of equal or higher position in role hierarchy.", null);
	}

	/**
	 * Throws a {@link MissingPermissionsException} if the given user is not higher in the role hierarchy than all of the given roles.
	 * This is determined by the position of the user's highest role and the position of the highest role in roles.
	 *
	 * @param guild The guild both users and roles belong to.
	 * @param user The user who must be higher in the role hierarchy.
	 * @param roles The roles which must be lower in the role hierarchy.
	 */
	public static void requireUserHigher(IGuild guild, IUser user, List<IRole> roles) {
		if (!isUserHigher(guild, user, roles))
			throw new MissingPermissionsException("Attempt to interact with user of equal or higher position in role hierarchy.", null);
	}

	/**
	 * Determines if user1 is higher in the role hierarchy than user2 in the given guild.
	 * This is determined by the positions of each of the users' highest roles.
	 *
	 * @param guild The guild both users belongs to.
	 * @param user1 The user who must be higher in the role hierarchy.
	 * @param user2 The user who must be lower in the role hierarchy.
	 * @return True if user1 is higher in the role hierarchy than user2.
	 */
	public static boolean isUserHigher(IGuild guild, IUser user1, IUser user2) {
		if (guild.getOwner().equals(user1)) return true;
		if (guild.getOwner().equals(user2)) return false;

		return hasHigherRoles(guild.getRolesForUser(user1), guild.getRolesForUser(user2));
	}

	/**
	 * Determines if the given user is higher in the role hierarchy than all of the given roles.
	 * This is determined by the position of the user's highest role and the position of the highest role in roles.
	 *
	 * @param guild The guild the user and roles belong to.
	 * @param user The user who must be higher in the role hierarchy.
	 * @param roles The roles which must be lower in the role hierarchy.
	 * @return True if the user is higher in the role hierarchy than every role in roles.
	 */
	public static boolean isUserHigher(IGuild guild, IUser user, List<IRole> roles) {
		if (guild.getOwner().equals(user)) return true;

		return hasHigherRoles(guild.getRolesForUser(user), roles);
	}

	/**
	 * Throws a {@link MissingPermissionsException} if the given user does not have all of the required permissions.
	 * This method takes into account if the user is the owner of the guild.
	 *
	 * @param guild The guild the user belongs to.
	 * @param user The user who must have all of the required permissions.
	 * @param required The permissions the user must have.
	 */
	public static void requirePermissions(IGuild guild, IUser user, Permissions... required) {
		requirePermissions(guild, user, arrayToEnumSet(required));
	}

	/**
	 * Throws a {@link MissingPermissionsException} if the given user does not have all of the required permissions.
	 * This method takes into account if the user is the owner of the guild.
	 *
	 * @param guild The guild the user belongs to.
	 * @param user The user who must have all of the required permissions.
	 * @param required The permissions the user must have.
	 */
	public static void requirePermissions(IGuild guild, IUser user, EnumSet<Permissions> required) {
		EnumSet<Permissions> copy = required.clone();
		copy.removeAll(user.getPermissionsForGuild(guild));
		if (!copy.isEmpty()) throw new MissingPermissionsException(copy);
	}

	/**
	 * Throws a {@link MissingPermissionsException} if the given user does not have all of the required permissions.
	 * This method takes into account user and roles overrides in the given channel.
	 *
	 * @param channel The channel to check.
	 * @param user The user who must have all of the required permissions.
	 * @param required The permissions the user must have.
	 */
	public static void requirePermissions(IChannel channel, IUser user, Permissions... required) {
		requirePermissions(channel, user, arrayToEnumSet(required));
	}

	/**
	 * Throws a {@link MissingPermissionsException} if the given user does not have all of the required permissions.
	 * This method takes into account user and roles overrides in the given channel.
	 *
	 * @param channel The channel to check.
	 * @param user The user who must have all of the required permissions.
	 * @param required The permissions the user must have.
	 */
	public static void requirePermissions(IChannel channel, IUser user, EnumSet<Permissions> required) {
		requirePermissions(channel.getModifiedPermissions(user), required);
	}

	/**
	 * Throws a {@link MissingPermissionsException} if the set of permissions does not contain all of the required permissions.
	 *
	 * @param permissions The permissions to check.
	 * @param required The permissions the given set must have.
	 */
	public static void requirePermissions(EnumSet<Permissions> permissions, EnumSet<Permissions> required) {
		EnumSet<Permissions> copy = required.clone();
		copy.removeAll(permissions);
		if (!copy.isEmpty()) throw new MissingPermissionsException(copy);
	}

	/**
	 * Determines if the given user has all of the required permissions.
	 * This method takes into account if the user is the owner of the guild.
	 *
	 * @param guild The guild the user belongs to.
	 * @param user The user who must have all of the required permissions.
	 * @param required The permissions the user must have.
	 * @return True if the user has all of the required permissions.
	 */
	public static boolean hasPermissions(IGuild guild, IUser user, Permissions... required) {
		return hasPermissions(guild, user, arrayToEnumSet(required));
	}

	/**
	 * Determines if the given user has all of the required permissions.
	 * This method takes into account if the user is the owner of the guild.
	 *
	 * @param guild The guild the user belongs to.
	 * @param user The user who must have all of the required permissions.
	 * @param required The permissions the user must have.
	 * @return True if the user has all of the required permissions.
	 */
	public static boolean hasPermissions(IGuild guild, IUser user, EnumSet<Permissions> required) {
		return user.getPermissionsForGuild(guild).containsAll(required);
	}

	/**
	 * Determines if the given user has all of the required permissions.
	 * This method takes into account user and roles overrides in the given channel.
	 *
	 * @param channel The channel to check.
	 * @param user The user who must have all of the required permissions.
	 * @param required The permissions the user must have.
	 * @return True if the user has all of the required permissions.
	 */
	public static boolean hasPermissions(IChannel channel, IUser user, Permissions... required) {
		return hasPermissions(channel, user, arrayToEnumSet(required));
	}

	/**
	 * Determines if the given user has all of the required permissions.
	 * This method takes into account user and roles overrides in the given channel.
	 *
	 * @param channel The channel to check.
	 * @param user The user who must have all of the required permissions.
	 * @param required The permissions the user must have.
	 * @return True if the user has all of the required permissions.
	 */
	public static boolean hasPermissions(IChannel channel, IUser user, EnumSet<Permissions> required) {
		return hasPermissions(channel.getModifiedPermissions(user), required);
	}

	/**
	 * Determines if the given set of permissions has all of the required permissions.
	 *
	 * @param permissions The permissions to check.
	 * @param required The permissions the given set must have.
	 * @return True if the given set of permissions has all of the required permissions.
	 */
	public static boolean hasPermissions(EnumSet<Permissions> permissions, EnumSet<Permissions> required) {
		return permissions.containsAll(required);
	}

	/**
	 * Throws a {@link MissingPermissionsException} if user1 is not higher in the role hierarchy than user2 in the given guild
	 * or user1 does not have all of the required permissions.
	 * The former is determined by the positions of each of the users' highest roles.
	 *
	 * @param guild The guild both users belong to.
	 * @param user1 The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param user2 The user who must be lower in the role hierarchy.
	 * @param required The permissions user1 must have.
	 */
	public static void requireHierarchicalPermissions(IGuild guild, IUser user1, IUser user2, Permissions... required) {
		requireHierarchicalPermissions(guild, user1, user2, arrayToEnumSet(required));
	}

	/**
	 * Throws a {@link MissingPermissionsException} if user1 is not higher in the role hierarchy than user2 in the given guild
	 * or user1 does not have all of the required permissions.
	 * The former is determined by the positions of each of the users' highest roles.
	 *
	 * @param guild The guild both users belong to.
	 * @param user1 The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param user2 The user who must be lower in the role hierarchy.
	 * @param required The permissions user1 must have.
	 */
	public static void requireHierarchicalPermissions(IGuild guild, IUser user1, IUser user2, EnumSet<Permissions> required) {
		requirePermissions(guild, user1, required);
		requireUserHigher(guild, user1, user2);
	}

	/**
	 * Throws a {@link MissingPermissionsException} if the given user is not higher in the role hierarchy than all of the given roles
	 * or the user does not have all of the required permissions.
	 * The former is determined by the position of the user's highest role and the position of the highest role in roles.
	 *
	 * @param guild The guild the user belongs to.
	 * @param user The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param roles The roles which must be lower in the role hierarchy.
	 * @param required The permissions the user must have.
	 */
	public static void requireHierarchicalPermissions(IGuild guild, IUser user, List<IRole> roles, Permissions... required) {
		requireHierarchicalPermissions(guild, user, roles, arrayToEnumSet(required));
	}

	/**
	 * Throws a {@link MissingPermissionsException} if the given user is not higher in the role hierarchy than all of the given roles
	 * or the user does not have all of the required permissions.
	 * The former is determined by the position of the user's highest role and the position of the highest role in roles.
	 *
	 * @param guild The guild the user belongs to.
	 * @param user The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param roles The roles which must be lower in the role hierarchy.
	 * @param required The permissions the user must have.
	 */
	public static void requireHierarchicalPermissions(IGuild guild, IUser user, List<IRole> roles, EnumSet<Permissions> required) {
		requirePermissions(guild, user, required);
		requireUserHigher(guild, user, roles);
	}

	/**
	 * Throws a {@link MissingPermissionsException} if user1 is not higher in the role hierarchy than user2 in the given channel's guild
	 * or user1 does not have all of the required permissions.
	 * The former is determined by the positions of each of the users' highest roles.
	 * This method takes into account user and roles overrides in the given channel.
	 *
	 * @param channel The channel to check.
	 * @param user1 The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param user2 The user who must be lower in the role hierarchy.
	 * @param required The permissions user1 must have.
	 */
	public static void requireHierarchicalPermissions(IChannel channel, IUser user1, IUser user2, Permissions... required) {
		requireHierarchicalPermissions(channel, user1, user2, arrayToEnumSet(required));
	}

	/**
	 * Throws a {@link MissingPermissionsException} if user1 is not higher in the role hierarchy than user2 in the given channel's guild
	 * or user1 does not have all of the required permissions.
	 * The former is determined by the positions of each of the users' highest roles.
	 * This method takes into account user and roles overrides in the given channel.
	 *
	 * @param channel The channel to check.
	 * @param user1 The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param user2 The user who must be lower in the role hierarchy.
	 * @param required The permissions user1 must have.
	 */
	public static void requireHierarchicalPermissions(IChannel channel, IUser user1, IUser user2, EnumSet<Permissions> required) {
		requirePermissions(channel, user1, required);
		requireUserHigher(channel.getGuild(), user1, user2);
	}

	/**
	 * Throws a {@link MissingPermissionsException} if the given user is not higher in the role hierarchy than all of the given roles
	 * or the user does not have all of the required permissions.
	 * The former is determined by the position of the user's highest role and the position of the highest role in roles.
	 * This method takes into account user and roles overrides in the given channel.
	 *
	 * @param channel The channel to check.
	 * @param user The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param roles The roles which must be lower in the role hierarchy.
	 * @param required The permissions the user must have.
	 */
	public static void requireHierarchicalPermissions(IChannel channel, IUser user, List<IRole> roles, Permissions... required) {
		requireHierarchicalPermissions(channel, user, roles, arrayToEnumSet(required));
	}

	/**
	 * Throws a {@link MissingPermissionsException} if the given user is not higher in the role hierarchy than all of the given roles
	 * or the user does not have all of the required permissions.
	 * The former is determined by the position of the user's highest role and the position of the highest role in roles.
	 * This method takes into account user and roles overrides in the given channel.
	 *
	 * @param channel The channel to check.
	 * @param user The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param roles The roles which must be lower in the role hierarchy.
	 * @param required The permissions the user must have.
	 */
	public static void requireHierarchicalPermissions(IChannel channel, IUser user, List<IRole> roles, EnumSet<Permissions> required) {
		requirePermissions(channel, user, required);
		requireUserHigher(channel.getGuild(), user, roles);
	}

	/**
	 * Determines if user1 is higher in the role hierarchy than user2 in the given guild
	 * and user1 has all of the required permissions.
	 * The former is determined by the positions of each of the users' highest roles.
	 *
	 * @param guild The guild both users belong to.
	 * @param user1 The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param user2 The user who must be lower in the role hierarchy.
	 * @param required The permissions user1 must have.
	 * @return True if user1 is higher in the role hierarchy than user2 and user1 has all of the required permissions.
	 */
	public static boolean hasHierarchicalPermissions(IGuild guild, IUser user1, IUser user2, Permissions... required) {
		return hasHierarchicalPermissions(guild, user1, user2, arrayToEnumSet(required));
	}

	/**
	 * Determines if user1 is higher in the role hierarchy than user2 in the given guild
	 * and user1 has all of the required permissions.
	 * The former is determined by the positions of each of the users' highest roles.
	 *
	 * @param guild The guild both users belong to.
	 * @param user1 The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param user2 The user who must be lower in the role hierarchy.
	 * @param required The permissions user1 must have.
	 * @return True if user1 is higher in the role hierarchy than user2 and user1 has all of the required permissions.
	 */
	public static boolean hasHierarchicalPermissions(IGuild guild, IUser user1, IUser user2, EnumSet<Permissions> required) {
		return hasPermissions(guild, user1, required) && isUserHigher(guild, user1, user2);
	}

	/**
	 * Determines if the given user is higher in the role hierarchy than all of the given roles
	 * and the user has all of the required permissions.
	 * The former is determined by the position of the user's highest role and the position of the highest role in roles.
	 *
	 * @param guild The guild the user belongs to.
	 * @param user The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param roles The roles which must be lower in the role hierarchy.
	 * @param required The permissions the user must have.
	 * @return True if the user is higher in the role hierarchy than every role in roles and the user has all of the required permissions.
	 */
	public static boolean hasHierarchicalPermissions(IGuild guild, IUser user, List<IRole> roles, Permissions... required) {
		return hasHierarchicalPermissions(guild, user, roles, arrayToEnumSet(required));
	}

	/**
	 * Determines if the given user is higher in the role hierarchy than all of the given roles
	 * and the user has all of the required permissions.
	 * The former is determined by the position of the user's highest role and the position of the highest role in roles.
	 *
	 * @param guild The guild the user belongs to.
	 * @param user The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param roles The roles which must be lower in the role hierarchy.
	 * @param required The permissions the user must have.
	 * @return True if the user is higher in the role hierarchy than every role in roles and the user has all of the required permissions.
	 */
	public static boolean hasHierarchicalPermissions(IGuild guild, IUser user, List<IRole> roles, EnumSet<Permissions> required) {
		return hasPermissions(guild, user, required) && isUserHigher(guild, user, roles);
	}

	/**
	 * Determines if user1 is higher in the role hierarchy than user2 in the given channel's guild
	 * and user1 has all of the required permissions.
	 * The former is determined by the positions of each of the users' highest roles.
	 * This method takes into account user and roles overrides in the given channel.
	 *
	 * @param channel The channel to check.
	 * @param user1 The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param user2 The user who must be lower in the role hierarchy.
	 * @param required The permissions user1 must have.
	 * @return True if user1 is higher in the role hierarchy than user2 and user1 has all of the required permissions.
	 */
	public static boolean hasHierarchicalPermissions(IChannel channel, IUser user1, IUser user2, Permissions... required) {
		return hasHierarchicalPermissions(channel, user1, user2, arrayToEnumSet(required));
	}

	/**
	 * Determines if user1 is higher in the role hierarchy than user2 in the given channel's guild
	 * and user1 has all of the required permissions.
	 * The former is determined by the positions of each of the users' highest roles.
	 * This method takes into account user and roles overrides in the given channel.
	 *
	 * @param channel The channel to check.
	 * @param user1 The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param user2 The user who must be lower in the role hierarchy.
	 * @param required The permissions user1 must have.
	 * @return True if user1 is higher in the role hierarchy than user2 and user1 has all of the required permissions.
	 */
	public static boolean hasHierarchicalPermissions(IChannel channel, IUser user1, IUser user2, EnumSet<Permissions> required) {
		return hasPermissions(channel, user1, required) && isUserHigher(channel.getGuild(), user1, user2);
	}

	/**
	 * Determines if the given user is higher in the role hierarchy than all of the given roles
	 * and the user has all of the required permissions.
	 * The former is determined by the position of the user's highest role and the position of the highest role in roles.
	 * This method takes into account user and roles overrides in the given channel.
	 *
	 * @param channel The channel to check.
	 * @param user The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param roles The roles which must be lower in the role hierarchy.
	 * @param required The permissions the user must have.
	 * @return True if the user is higher in the role hierarchy than every role in roles and the user has all of the required permissions.
	 */
	public static boolean hasHierarchicalPermissions(IChannel channel, IUser user, List<IRole> roles, Permissions... required) {
		return hasHierarchicalPermissions(channel, user, roles, arrayToEnumSet(required));
	}

	/**
	 * Determines if the given user is higher in the role hierarchy than all of the given roles
	 * and the user has all of the required permissions.
	 * The former is determined by the position of the user's highest role and the position of the highest role in roles.
	 * This method takes into account user and roles overrides in the given channel.
	 *
	 * @param channel The channel to check.
	 * @param user The user who must be higher in the role hierarchy and have all of the required permissions.
	 * @param roles The roles which must be lower in the role hierarchy.
	 * @param required The permissions the user must have.
	 * @return True if the user is higher in the role hierarchy than every role in roles and the user has all of the required permissions.
	 */
	public static boolean hasHierarchicalPermissions(IChannel channel, IUser user, List<IRole> roles, EnumSet<Permissions> required) {
		return hasPermissions(channel, user, required) && isUserHigher(channel.getGuild(), user, roles);
	}

	/**
	 * Gets the permissions a user has after applying specific and role overrides.
	 *
	 * @param user The user to get permissions for.
	 * @param guild The guild the user is in.
	 * @param userOverrides The internal user overrides cache.
	 * @param roleOverrides The internal role overrides cache.
	 * @return The permissions the user has in the permissions object.
	 */
	public static EnumSet<Permissions> getModifiedPermissions(IUser user, IGuild guild,
	                                                          Cache<PermissionOverride> userOverrides,
	                                                          Cache<PermissionOverride> roleOverrides) {
		EnumSet<Permissions> base = user.getPermissionsForGuild(guild);
		if (base.contains(Permissions.ADMINISTRATOR)) {
			return EnumSet.allOf(Permissions.class);
		}

		PermissionOverride everyoneOverride = roleOverrides.get(guild.getLongID());
		if (everyoneOverride != null) {
			base.retainAll(EnumSet.complementOf(everyoneOverride.deny()));
			base.addAll(everyoneOverride.allow());
		}

		EnumSet<Permissions> allow = EnumSet.noneOf(Permissions.class);
		EnumSet<Permissions> deny = EnumSet.noneOf(Permissions.class);

		for (IRole role : guild.getRolesForUser(user)) {
			PermissionOverride roleOverride = roleOverrides.get(role.getLongID());
			if (roleOverride != null) {
				allow.addAll(roleOverride.allow());
				deny.addAll(roleOverride.deny());
			}
		}

		base.retainAll(EnumSet.complementOf(deny));
		base.addAll(allow);

		PermissionOverride userOverride = userOverrides.get(user.getLongID());
		if (userOverride != null) {
			base.retainAll(EnumSet.complementOf(userOverride.deny()));
			base.addAll(userOverride.allow());
		}

		return base;
	}

	/**
	 * Gets the permissions a role has after applying overrides.
	 *
	 * @param role The role to get permissions for.
	 * @param roleOverrides The internal role overrides cache.
	 * @return The permissions the role has in the permissions object.
	 */
	public static EnumSet<Permissions> getModifiedPermissions(IRole role, Cache<PermissionOverride> roleOverrides) {
		EnumSet<Permissions> base = role.getPermissions();
		PermissionOverride override = roleOverrides.get(role.getLongID());

		if (override == null) {
			if ((override = roleOverrides.get(role.getGuild().getEveryoneRole().getLongID())) == null)
				return base;
		}

		base.addAll(new ArrayList<>(override.allow()));
		override.deny().forEach(base::remove);

		return base;
	}

	/**
	 * Determines if the position of roles1's highest role is greater than the position of roles2's highest role.
	 *
	 * @param roles1 The list of roles whose highest role's position must be greater.
	 * @param roles2 The list of roles whose highest role's position must be less.
	 * @return True if the position of role1s's highest role is greater than the position of roles2's highest role.
	 */
	private static boolean hasHigherRoles(List<IRole> roles1, List<IRole> roles2) {
		OptionalInt maxPos1 = roles1.stream().mapToInt(IRole::getPosition).max();
		OptionalInt maxPos2 = roles2.stream().mapToInt(IRole::getPosition).max();

		return (maxPos1.isPresent() ? maxPos1.getAsInt() : 0) > (maxPos2.isPresent() ? maxPos2.getAsInt() : 0);
	}

	/**
	 * Converts an array of {@link Permissions} to an {@link EnumSet<Permissions>}.
	 *
	 * @param array The array of permissions.
	 * @return An EnumSet of the passed permissions.
	 */
	private static EnumSet<Permissions> arrayToEnumSet(Permissions... array) {
		EnumSet<Permissions> set = EnumSet.noneOf(Permissions.class);
		set.addAll(Arrays.asList(array));
		return set;
	}
}
