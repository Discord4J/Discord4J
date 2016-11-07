package sx.blah.discord.handle.obj;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Defines a Discord webhook for a channel.
 */
public interface IWebhook extends IDiscordObject<IWebhook> {

	/**
	 * Gets the ID of this webhook.
	 *
	 * @return The webhook ID.
	 */
	String getID();

	/**
	 * Gets the guild/server this webhook belongs to.
	 *
	 * @return The guild.
	 */
	IGuild getGuild();

	/**
	 * Gets the channel this webhook belongs to.
	 *
	 * @return The channel.
	 */

	IChannel getChannel();

	/**
	 * Gets the author of this webhook.
	 *
	 * @return The author.
	 */

	IUser getAuthor();

	/**
	 * Gets the default name of this webhook.
	 *
	 * @return The default name.
	 */

	String getName();

	/**
	 * Gets the default avatar of this webhook.
	 *
	 * @return The default avatar.
	 */

	String getAvatar();

	/**
	 * Gets the secure token of the webhook.
	 *
	 * @return The secure token.
	 */

	String getToken();

	/**
	 * Changes the default name of the user this webhook posts as.
	 *
	 * @param name The default name for the user.
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */

	void changeName(String name) throws RateLimitException, DiscordException, MissingPermissionsException;

	/**
	 * Changes the default avatar of the user this webhook posts as.
	 *
	 * @param avatar The default avatar, as encoded in base64. For URLs, use {@link Image#forUrl(String, String)} and {@link #changeAvatar(Image)}
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeAvatar(String avatar) throws RateLimitException, DiscordException, MissingPermissionsException;

	/**
	 * Changes the default avatar of the user this webhook posts as.
	 *
	 * @param avatar The Image object to use as the avatar.
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */

	void changeAvatar(Image avatar) throws RateLimitException, DiscordException, MissingPermissionsException;

	/**
	 * Deletes this webhook.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void delete() throws MissingPermissionsException, RateLimitException, DiscordException;

	/**
	 * Executes this webhook with a simple message.
	 *
	 * @param content The message to post.
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void execute(String content) throws RateLimitException, DiscordException;

	/**
	 * Executes this webhook with a simple TTS message, but a different username and avatar
	 *
	 * @param content   The message to post.
	 * @param username  The username to post under.
	 * @param avatarUrl The avatar url to use.
	 * @param tts       If the message should be posted as a TTS message or not.
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void execute(String content, String username, String avatarUrl, boolean tts) throws RateLimitException, DiscordException;

	/**
	 * Executes this webhook by uploading a file (8 MB limit).
	 *
	 * @param file The file to upload.
	 * @throws FileNotFoundException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void execute(File file) throws FileNotFoundException, RateLimitException, DiscordException;

	/**
	 * Executes this webhook by uploading a file (8 MB limit). Uses a different username and avatar to the default.
	 *
	 * @param file      The file to upload.
	 * @param username  The username to post under.
	 * @param avatarUrl The avatar url to use.
	 * @throws FileNotFoundException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void execute(File file, String username, String avatarUrl) throws FileNotFoundException, RateLimitException, DiscordException;

	/**
	 * Executes this webhook by uploading a file (8 MB limit) and posting a message.
	 *
	 * @param content The message to post.
	 * @param file    The file to upload.
	 * @throws FileNotFoundException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void execute(String content, File file) throws FileNotFoundException, RateLimitException, DiscordException;

	/**
	 * Executes this webhook by uploading a sequence of data (8 MB limit) with a filename.
	 *
	 * @param data     The InputStream to read data from, to upload.
	 * @param fileName The filename to post as.
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void execute(InputStream data, String fileName) throws RateLimitException, DiscordException;

	/**
	 * Executes this webhook by uploading a sequence of data (8 MB limit) with a filename, under a different username and avatar to the default.
	 *
	 * @param data      The InputStream to read data from, to upload.
	 * @param fileName  The filename to post as.
	 * @param username  The username to post under.
	 * @param avatarUrl The avatar url to use.
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void execute(InputStream data, String fileName, String username, String avatarUrl) throws RateLimitException, DiscordException;

	/**
	 * Executes this webhook by uploading a sequence of data (8 MB limit) with a filename, and sending a message.
	 *
	 * @param content  The message to post.
	 * @param data     The InputStream to read data from, to upload.
	 * @param fileName The filename to post as.
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void execute(String content, InputStream data, String fileName) throws RateLimitException, DiscordException;

	/**
	 * Executes this webhook by uploading a sequence of data (8 MB limit) with a filename, and sending a message, under a different username and avatar to the default.
	 *
	 * @param content   The message to post.
	 * @param data      The InputStream to read data from, to upload.
	 * @param fileName  The filename to post as.
	 * @param username  The username to post under.
	 * @param avatarUrl The avatar url to post under.
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void execute(String content, InputStream data, String fileName, String username, String avatarUrl) throws RateLimitException, DiscordException;

	/**
	 * Executes this webhook with an array of embedded content.
	 *
	 * @param content The array of embedded content.
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void execute(IEmbedded[] content) throws RateLimitException, DiscordException;

	/**
	 * Executes this webhook with an array of embedded content, under a different username and avatar to the default.
	 *
	 * @param content   The array of embedded content.
	 * @param username  The username to post under.
	 * @param avatarUrl The avatar url to post under.
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void execute(IEmbedded[] content, String username, String avatarUrl) throws RateLimitException, DiscordException;
}
