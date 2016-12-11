package sx.blah.discord.handle.obj;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

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
	 * Gets the status for this user.
	 *
	 * @return The user's status.
	 */
	Status getStatus();

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
	Presences getPresence();

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
	Optional<String> getNicknameForGuild(IGuild guild);

	/**
	 * Gets whether or not this user is a bot.
	 *
	 * @return True if a bot, false if otherwise.
	 */
	boolean isBot();

	/**
	 * Moves this user to a different voice channel.
	 *
	 * @param newChannel The new channel the user should move to.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	void moveToVoiceChannel(IVoiceChannel newChannel) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Gets the voice channels this user is connected to.
	 *
	 * @return The voice channels.
	 */
	List<IVoiceChannel> getConnectedVoiceChannels();

	/**
	 * Gets a {@link IPrivateChannel} for this user.
	 *
	 * @return The {@link IPrivateChannel} object.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	IPrivateChannel getOrCreatePMChannel() throws RateLimitException, DiscordException;

	/**
	 * Gets whether this user is deafened in the given guild.
	 *
	 * @param guild The guild to check the status for.
	 * @return True if deafened, false if otherwise.
	 */
	boolean isDeaf(IGuild guild);

	/**
	 * Gets whether this user is muted in the given guild.
	 *
	 * @param guild The guild to check the status for.
	 * @return True if muted, false if otherwise.
	 */
	boolean isMuted(IGuild guild);

	/**
	 * Gets whether this user is deafened locally (meaning they deafened themselves).
	 *
	 * @return True if deafened, false if otherwise.
     */
	boolean isDeafLocally();

	/**
	 * Gets whether this user is muted locally (meaning they muted themselves).
	 *
	 * @return True if muted, false if otherwise.
     */
	boolean isMutedLocally();

	/**
	 * Adds a Role to this user.
	 *
	 * @param role The role to add to the User
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	void addRole(IRole role) throws MissingPermissionsException, RateLimitException, DiscordException;


	/**
	 * Removes a Role from this user.
	 *
	 * @param role The role to remove from the User.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	void removeRole(IRole role) throws MissingPermissionsException, RateLimitException, DiscordException;
}
