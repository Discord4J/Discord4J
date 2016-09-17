package sx.blah.discord.handle.obj;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.Image;

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
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void changeName(String name) throws RateLimitException, DiscordException;

	/**
	 * Changes the application's description.
	 *
	 * @param description The new description.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void changeDescription(String description) throws RateLimitException, DiscordException;

	/**
	 * Changes the application's icon.
	 *
	 * @param icon The new icon, or deletes the icon if null.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void changeIcon(Image icon) throws RateLimitException, DiscordException;

	/**
	 * Changes the application's redirectUris.
	 *
	 * @param redirectUris The new redirectUris.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void changeRedirectUris(String[] redirectUris) throws RateLimitException, DiscordException;

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
