/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package sx.blah.discord.handle.obj;

import sx.blah.discord.util.cache.LongMap;

import java.util.EnumSet;
import java.util.List;

/**
 * A category in Discord.
 */
public interface ICategory extends IDiscordObject<ICategory> {

	/**
	 * Deletes the category.
	 */
	void delete();

	/**
	 * Gets whether the category is deleted.
	 *
	 * @return Whether the category is deleted.
	 */
	boolean isDeleted();

	/**
	 * Gets the category's text channels.
	 *
	 * @return The category's text channels.
	 */
	List<IChannel> getChannels();

	/**
	 * Gets the category's voice channels.
	 *
	 * @return The category's voice channels.
	 */
	List<IVoiceChannel> getVoiceChannels();

	/**
	 * Creates a new channel, initially adding it to this category.
	 *
	 * @param name The name of the new channel. MUST be between 2-100 characters long.
	 * @return The new channel.
	 */
	IChannel createChannel(String name);

	/**
	 * Creates a new voice channel, initially adding it to this category.
	 *
	 * @param name The name of the new voice channel. MUST be between 2-100 characters long.
	 * @return The new voice channel.
	 */
	IVoiceChannel createVoiceChannel(String name);

	/**
	 * Gets the parent guild of the category.
	 *
	 * @return The parent guild of the category.
	 */
	IGuild getGuild();

	/**
	 * Gets the position of the category in the channel list.
	 *
	 * @return The position of the category in the channel list.
	 */
	int getPosition();

	/**
	 * Changes the position of the category.
	 *
	 * @param position The position of the category.
	 */
	void changePosition(int position);

	/**
	 * Gets the name of the category.
	 *
	 * @return The name of the category.
	 */
	String getName();

	/**
	 * Changes the name of the category.
	 *
	 * @param name The name of the category.
	 */
	void changeName(String name);

	/**
	 * Gets whether the category is marked as NSFW (Not Safe For Work).
	 *
	 * @return Whether the category is marked as NSFW.
	 */
	boolean isNSFW();

	/**
	 * Changes the nsfw state of the category.
	 *
	 * @param isNSFW The new nsfw state of the category.
	 */
	void changeNSFW(boolean isNSFW);

	/**
	 * Gets the permissions a user has in the category, taking into account user and role overrides.
	 *
	 * @param user The user to get permissions for.
	 * @return The permissions the user has in the category.
	 */
	EnumSet<Permissions> getModifiedPermissions(IUser user);

	/**
	 * Gets the permissions a role has in the category, taking into account role overrides.
	 *
	 * @param role The role to get permissions for.
	 * @return The permissions the role has in the category.
	 */
	EnumSet<Permissions> getModifiedPermissions(IRole role);

	/**
	 * Gets the permissions overrides for users. (Key = User ID)
	 *
	 * @return The user permissions overrides for the category.
	 */
	LongMap<PermissionOverride> getUserOverrides();

	/**
	 * Gets the permissions overrides for roles. (Key = Role ID)
	 *
	 * @return The role permissions overrides for this category.
	 */
	LongMap<PermissionOverride> getRoleOverrides();

	/**
	 * Removes a user's permissions override.
	 *
	 * @param user The user whose override should be removed.
	 */
	void removePermissionsOverride(IUser user);

	/**
	 * Removes a role's permissions override.
	 *
	 * @param role The role whose override should be removed.
	 */
	void removePermissionsOverride(IRole role);

	/**
	 * Creates or edits a role's permissions override.
	 *
	 * @param role The role to create/edit the permissions override for.
	 * @param toAdd The permissions to explicitly grant.
	 * @param toRemove The permissions to explicitly deny.
	 */
	void overrideRolePermissions(IRole role, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove);

	/**
	 * Creates or edits a user's permissions override.
	 *
	 * @param user The user to create/edit the permissions override for.
	 * @param toAdd The permissions to explicitly grant.
	 * @param toRemove The permissions to explicitly deny.
	 */
	void overrideUserPermissions(IUser user, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove);
}
