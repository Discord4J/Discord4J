package sx.blah.discord.handle.obj;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.Image;

import java.util.EnumSet;
import java.util.Optional;

/**
 * This represents a Discord application.
 */
public interface IApplication extends IDiscordObject<IApplication> {

	/**
	 * Gets the application's OAuth client secret key.
	 *
	 * @return The secret key.
	 */
	String getSecret();

	/**
	 * Gets the OAuth redirect uris for the application.
	 *
	 * @return The redirect uris.
	 */
	String[] getRedirectUris();

	/**
	 * Gets the application description.
	 *
	 * @return The description.
	 */
	String getDescription();

	/**
	 * Gets the name of the application.
	 *
	 * @return The name.
	 */
	String getName();

	/**
	 * The application icon.
	 *
	 * @return The icon id.
	 */
	String getIcon();

	/**
	 * The application icon url.
	 *
	 * @return The icon url.
	 */
	String getIconUrl();

	/**
	 * Changes the application's name.
	 *
	 * @param name The new name.
	 *
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 */
	void changeName(String name) throws HTTP429Exception, DiscordException;

	/**
	 * Changes the application's description.
	 *
	 * @param description The new description.
	 *
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 */
	void changeDescription(String description) throws HTTP429Exception, DiscordException;

	/**
	 * Changes the application's icon.
	 *
	 * @param icon The new icon.
	 *
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 */
	void changeIcon(Optional<Image> icon) throws HTTP429Exception, DiscordException;

	/**
	 * Changes the application's redirectUris.
	 *
	 * @param redirectUris The new redirectUris.
	 *
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 */
	void changeRedirectUris(String[] redirectUris) throws HTTP429Exception, DiscordException;

	/**
	 * Converts the provided user into a bot by token.
	 * WARNING: Converting a user account to a bot account is irreversible.
	 *
	 * @param token The user's token.
	 * @return The (now bot) user's new token.
	 *
	 * @throws DiscordException
	 */
	String convertUserToBot(String token) throws DiscordException;

	/**
	 * Converts the provided user into a bot by client. NOTE: The client is automatically converted to a bot client.
	 * WARNING: Converting a user account to a bot account is irreversible.
	 *
	 * @param client The user's client.
	 * @return The (now bot) user's new token.
	 *
	 * @throws DiscordException
	 */
	String convertUserToBot(IDiscordClient client) throws DiscordException;

	/**
	 * Creates a new Bot account for the application. Your application name will be used as the name of the bot.
	 *
	 * @return The new client builder pre-loaded with the bot's token.
	 *
	 * @throws DiscordException
	 */
	ClientBuilder createBot() throws DiscordException;

	/**
	 * Deletes the application.
	 *
	 * @throws DiscordException
	 */
	void delete() throws DiscordException;

	/**
	 * Generates a bot invite url for this application.
	 *
	 * @param requestedPermissions The (optional) requested permissions for the bot.
	 * @param guildID The (optional) specific guild for the bot to be added to.
	 * @return The url.
	 */
	String createBotInvite(Optional<EnumSet<Permissions>> requestedPermissions, Optional<String> guildID);

	/**
	 * Gets the bot owned by this application (if it exists).
	 *
	 * @return The bot user object.
	 */
	IUser getBotUser();

	/**
	 * Gets the authorization token for the bot tied to this application.
	 *
	 * @return The token.
	 */
	String getBotToken();
}
