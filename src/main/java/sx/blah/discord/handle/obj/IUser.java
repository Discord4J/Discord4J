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

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.cache.LongMap;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class defines the Discord user.
 */
public interface IUser extends IDiscordObject<IUser> {

	/**
	 * Gets the user's username.
	 *
	 * @return The username.
	 */
	String getName();

	/**
	 * Gets the user's avatar id.
	 *
	 * @return The avatar id.
	 */
	String getAvatar();

	/**
	 * Gets the user's avatar direct link.
	 *
	 * @return The avatar url.
	 */
	String getAvatarURL();

	/**
	 * Gets the user's presence.
	 *
	 * @return The user's presence.
	 */
	IPresence getPresence();

	/**
	 * Gets the status for this user.
	 *
	 * @return The user's status.
	 * @deprecated Use {@link #getPresence()}
	 */
	@Deprecated
	Status getStatus();

	/**
	 * Gets the name displayed to a guild for this user.
	 *
	 * @param guild The guild to check the display name for.
	 * @return The display name. This is the user's nickname if it exists, otherwise the user's standard name.
	 */
	String getDisplayName(IGuild guild);

	/**
	 * Formats a string to @mention the user.
	 * NOTE: This is equivalent to mention(true).
	 *
	 * @return The formatted string.
	 */
	String mention();

	/**
	 * Formats a string to @mention the user.
	 *
	 * @param mentionWithNickname If true, the mention will display the user's nickname instead of the user's "real"
	 * name if it exists.
	 * @return The formatted string.
	 */
	String mention(boolean mentionWithNickname);

	/**
	 * Gets the discriminator for the user. This is used by Discord to differentiate between two users with the same name.
	 *
	 * @return The discriminator.
	 */
	String getDiscriminator();

	/**
	 * Gets the roles the user is a part of.
	 *
	 * @param guild The guild to check the roles for.
	 * @return The roles.
	 */
	List<IRole> getRolesForGuild(IGuild guild);

	/**
	 * Gets the permissions the user has on the guild.
	 *
	 * @param guild The guild to check the permissions for.
	 * @return The permissions.
       */
	EnumSet<Permissions> getPermissionsForGuild(IGuild guild);

	/**
	 * Gets the nickname for this user in this guild.
	 *
	 * @param guild The guild to get the nickname for.
	 * @return The nickname (if it exists in this guild).
	 */
	String getNicknameForGuild(IGuild guild);

	/**
	 * Get's the user's never-null voice state for the given guild.
	 *
	 * @param guild The guild to check.
	 * @return The voice state.
	 */
	IVoiceState getVoiceStateForGuild(IGuild guild);

	/**
	 * Gets all of the user's voice states.
	 * Key is the guild ID that the voice state is for.
	 *
	 * @return All of the user's voice states.
	 * @deprecated Use {@link #getVoiceStatesLong()} instead
	 */
	@Deprecated
	default Map<String, IVoiceState> getVoiceStates() {
		Map<String, IVoiceState> map = new HashMap<>();
		getVoiceStatesLong().forEach((key, value) -> map.put(Long.toUnsignedString(key), value));
		return map;
	}

	/**
	 * Gets all of the user's voice states.
	 * Key is the guild ID that the voice state is for.
	 *
	 * @return All of the user's voice states.
	 */
	LongMap<IVoiceState> getVoiceStatesLong();

	/**
	 * Moves the user to the given voice channel.
	 *
	 * @param channel The voice channel to move to.
	 */
	void moveToVoiceChannel(IVoiceChannel channel);

	/**
	 * Gets whether or not this user is a bot.
	 *
	 * @return True if a bot, false if otherwise.
	 */
	boolean isBot();

	/**
	 * Gets a {@link IPrivateChannel} for this user.
	 *
	 * @return The {@link IPrivateChannel} object.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	IPrivateChannel getOrCreatePMChannel();

	/**
	 * Adds a Role to this user.
	 *
	 * @param role The role to add to the User
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	void addRole(IRole role);

	/**
	 * Removes a Role from this user.
	 *
	 * @param role The role to remove from the User.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	void removeRole(IRole role);
}
