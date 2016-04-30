package sx.blah.discord.handle.obj;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

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
	 * Formats a string to @mention the user.
	 *
	 * @return The formatted string.
	 */
	String mention();

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
}
