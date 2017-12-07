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

import sx.blah.discord.util.cache.LongMap;

import java.awt.Color;
import java.util.EnumSet;
import java.util.List;

/**
 * A Discord user.
 */
public interface IUser extends IDiscordObject<IUser> {

	/**
	 * Gets the user's name.
	 *
	 * @return The user's name.
	 */
	String getName();

	/**
	 * Gets the user's avatar hash.
	 *
	 * @return The user's avatar hash.
	 */
	String getAvatar();

	/**
	 * Gets the user's avatar URL.
	 *
	 * @return The user's avatar URL.
	 */
	String getAvatarURL();

	/**
	 * Gets the user's presence.
	 *
	 * @return The user's presence.
	 */
	IPresence getPresence();

	/**
	 * Gets the user's display name. This is their nickname or their username if they do not have a nickname.
	 *
	 * @param guild The guild to get their display name for.
	 * @return The user's display name.
	 */
	String getDisplayName(IGuild guild);

	/**
	 * Gets a formatted string mentioning the user.
	 *
	 * <p>This is equivalent to <code>mention(true)</code>
	 *
	 * @return A formatted string mentioning the user.
	 */
	String mention();

	/**
	 * Gets a formatted string mentioning the user.
	 *
	 * @param mentionWithNickname Whether the mention should display the user's nickname or their username.
	 * @return A formatted string mentioning the user.
	 */
	String mention(boolean mentionWithNickname);

	/**
	 * Gets the user's discriminator.
	 *
	 * @return The user's discriminator.
	 */
	String getDiscriminator();

	/**
	 * Gets the roles the user has in the given guild.
	 *
	 * @param guild The guild to get roles for.
	 * @return The roles the user has in the given guild.
	 */
	List<IRole> getRolesForGuild(IGuild guild);

	/**
	 * Gets the color the user's name is shown as in the given guild.
	 *
	 * @param guild The guild to get roles for.
	 * @return The color the user has in the given guild.
	 */
	Color getColorForGuild(IGuild guild);

	/**
	 * Gets the permissions the user has in the given guild.
	 *
	 * @param guild The guild to get permissions for.
	 * @return The permissions the user has in the given guild.
       */
	EnumSet<Permissions> getPermissionsForGuild(IGuild guild);

	/**
	 * Gets the user's nickname in the given guild.
	 *
	 * @param guild The guild to get the nickname for.
	 * @return The user's nickname in the given guild (or null if they don't have one).
	 */
	String getNicknameForGuild(IGuild guild);

	/**
	 * Gets the user's voice state for the given guild.
	 *
	 * @param guild The guild to get the voice state for.
	 * @return The user's voice state for the given guild.
	 */
	IVoiceState getVoiceStateForGuild(IGuild guild);

	/**
	 * Gets the user's voice states for every guild. (Key = Guild ID).
	 *
	 * @return The user's voice states for every guild.
	 */
	LongMap<IVoiceState> getVoiceStates();

	/**
	 * Moves the user from one voice channel to another.
	 *
	 * @param channel The voice channel to move the user to.
	 */
	void moveToVoiceChannel(IVoiceChannel channel);

	/**
	 * Gets whether the user is a bot.
	 *
	 * @return Whether the user is a bot.
	 */
	boolean isBot();

	/**
	 * Gets the private channel for the user or creates it if one doesn't exist.
	 *
	 * @return The private channel for the user.
	 */
	IPrivateChannel getOrCreatePMChannel();

	/**
	 * Adds a role to the user.
	 *
	 * @param role The role to add.
	 */
	void addRole(IRole role);

	/**
	 * Removes a role from the user.
	 *
	 * @param role The role to remove.
	 */
	void removeRole(IRole role);

	/**
	 * Gets whether the user has a role.
	 *
	 * @param role The role to validate.
	 * @return True if the user has the role, false otherwise.
	 */
	boolean hasRole(IRole role);
}
