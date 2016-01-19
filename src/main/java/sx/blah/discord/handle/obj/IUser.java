package sx.blah.discord.handle.obj;

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
	 * @param guildID The guild id to check the roles for.
	 * @return The roles.
	 */
	List<IRole> getRolesForGuild(String guildID);
}
