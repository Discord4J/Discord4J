package sx.blah.discord.handle.obj;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * This class defines the Discord user.
 */
public interface IUser {

	/**
	 * Gets the user's unique id.
	 *
	 * @return The user's id.
	 */
	String getID();

	/**
	 * Gets the user's username.
	 *
	 * @return The username.
	 */
	String getName();

	/**
	 * Gets the game the user is playing, no value if the user isn't playing a game.
	 *
	 * @return The game.
	 */
	Optional<String> getGame();

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
	 * Gets the nickname for this user in this guild.
	 *
	 * @param guild The guild to get the nickname for.
	 * @return The nickname (if it exists in this guild).
	 */
	Optional<String> getNicknameForGuild(IGuild guild);

	/**
	 * This calculates the time at which this object has been created by analyzing its Discord ID.
	 *
	 * @return The time at which this object was created.
	 */
	LocalDateTime getCreationDate();

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
	 * @throws HTTP429Exception
	 * @throws MissingPermissionsException
	 */
	void moveToVoiceChannel(IVoiceChannel newChannel) throws DiscordException, HTTP429Exception, MissingPermissionsException;

	/**
	 * Gets the voice channel this user is in (if in one).
	 *
	 * @return The (optional) voice channel.
	 */
	Optional<IVoiceChannel> getVoiceChannel();

	/**
	 * This gets the client that this object is tied to.
	 *
	 * @return The client.
	 */
	IDiscordClient getClient();
}
