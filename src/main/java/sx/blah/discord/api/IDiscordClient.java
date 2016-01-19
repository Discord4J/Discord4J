package sx.blah.discord.api;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import sx.blah.discord.handle.EventDispatcher;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.HTTP403Exception;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;

/**
 * Represents the main discord api
 */
public interface IDiscordClient {
	
	/**
	 * Gets the {@link EventDispatcher} instance for this client. Use this to handle events.
	 *
	 * @return The event dispatcher instance.
	 */
	EventDispatcher getDispatcher();
	
	/**
	 * Gets the authorization token for this client.
	 *
	 * @return The authorization token.
	 */
	String getToken();
	
	/**
	 * Logs the client in as the provided account.
	 *
	 * @throws DiscordException This is thrown if there is an error logging in.
	 */
	void login() throws DiscordException;
	
	/**
	 * Logs out the client.
	 *
	 * @throws HTTP403Exception
	 */
	void logout() throws HTTP403Exception;
	
	/**
	 * Sends a message to the desired channel.
	 *
	 * @param content The content of the message.
	 * @param channelID The channel id of the channel to receive the message.
	 * @return The message object representing the sent message
	 *
	 * @throws IOException
	 * @deprecated Use {@link Channel#sendMessage(String)}
	 */
	@Deprecated
	IMessage sendMessage(String content, String channelID) throws IOException;
	
	/**
	 * Edits a message. NOTE: Discord only supports editing YOUR OWN messages!
	 *
	 * @param content The new content for the message to contain.
	 * @param messageID The message id of the message to edit.
	 * @param channelID The channel id of the channel the message belongs to.
	 * @return The new message.
	 *
	 * @deprecated Use {@link Message#edit(String)}
	 */
	@Deprecated
	IMessage editMessage(String content, String messageID, String channelID);
	
	/**
	 * Deletes a message.
	 *
	 * @param messageID The message id of the message to delete.
	 * @param channelID The channel id of the channel the message belongs to.
	 * @throws IOException
	 * @deprecated Use {@link Message#delete()}
	 */
	@Deprecated
	void deleteMessage(String messageID, String channelID) throws IOException;
	
	/**
	 * FIXME: Fix this because it's fucking stupid.
	 * Allows you to change the info on your bot.
	 * Any fields you don't want to change should be left as an empty string ("") or null.
	 *
	 * @param username Username (if you want to change it).
	 * @param email Email (if you want to change it)
	 * @param password Password (if you want to change it).
	 * @param avatar Image data for the bot's avatar, {@link Image}
	 */
	void changeAccountInfo(String username, String email, String password, Image avatar) throws UnsupportedEncodingException, URISyntaxException;
	
	/**
	 * Updates the bot's presence.
	 *
	 * @param isIdle If true, the bot will be "idle", otherwise the bot will be "online".
	 * @param game The optional name of the game the bot is playing. If empty, the bot simply won't be playing a game.
	 */
	void updatePresence(boolean isIdle, Optional<String> game);
	
	/**
	 * Checks if the api is ready to be interacted with (if it is logged in).
	 *
	 * @return True if ready, false if otherwise.
	 */
	boolean isReady();
	
	/**
	 * Gets the {@link User} this bot is representing.
	 *
	 * @return The user object.
	 */
	IUser getOurUser();
	
	/**
	 * Gets a channel by its unique id.
	 *
	 * @param channelID The id of the desired channel.
	 * @return The {@link Channel} object with the provided id.
	 */
	IChannel getChannelByID(String channelID);
	
	/**
	 * Gets a voice channel from a given id.
	 * 
	 * @param id The voice channel id.
	 * @return The voice channel (or null if not found).
	 */
	IVoiceChannel getVoiceChannelByID(String id);
	
	/**
	 * Gets a guild by its unique id.
	 *
	 * @param guildID The id of the desired guild.
	 * @return The {@link Guild} object with the provided id.
	 */
	IGuild getGuildByID(String guildID);
	
	/**
	 * Gets all the guilds the user the api represents is connected to.
	 *
	 * @return The list of {@link Guild}s the api is connected to.
	 */
	List<IGuild> getGuilds();
	
	/**
	 * Gets a user by its unique id.
	 *
	 * @param userID The id of the desired user.
	 * @return The {@link User} object with the provided id.
	 */
	IUser getUserByID(String userID);
	
	/**
	 * Gets a {@link PrivateChannel} for the provided recipient.
	 *
	 * @param user The user who will be the recipient of the private channel.
	 * @return The {@link PrivateChannel} object.
	 *
	 * @throws Exception
	 */
	IPrivateChannel getOrCreatePMChannel(IUser user) throws Exception;
	
	/**
	 * Toggles whether the bot is "typing".
	 *
	 * @param channelID The channel to maintain the typing status to.
	 * @deprecated Use {@link Channel#toggleTypingStatus()}
	 */
	@Deprecated
	void toggleTypingStatus(String channelID);
	
	/**
	 * Gets whether the bot is "typing".
	 *
	 * @param channelID The channel to get the typing status for for this bot.
	 * @return True if the bot is typing, false if otherwise.
	 *
	 * @deprecated Use {@link Channel#getTypingStatus()}
	 */
	@Deprecated
	boolean getTypingStatus(String channelID);
	
	/**
	 * Generates an invite for this channel.
	 *
	 * @param maxAge How long the invite should be valid, setting it to 0 makes it last forever.
	 * @param maxUses The maximum uses for the invite, setting it to 0 makes the invite have unlimited uses.
	 * @param temporary Whether users admitted with this invite are temporary.
	 * @param useXkcdPass Whether to generate a human-readable code, maxAge cannot be 0 for this to work.
	 * @param channelID The channel to get the invite for.
	 * @return The newly generated invite.
	 *
	 * @deprecated Use {@link Channel#createInvite(int, int, boolean, boolean)}
	 */
	@Deprecated
	IInvite createInvite(int maxAge, int maxUses, boolean temporary, boolean useXkcdPass, String channelID);
	
	/**
	 * Gets the invite for a code.
	 *
	 * @param code The invite code or xkcd pass.
	 * @return The invite, or null if it doesn't exist.
	 */
	IInvite getInviteForCode(String code);
	
	/**
	 * Creates a new channel.
	 *
	 * @param guild The guild to create the channel for.
	 * @param name The name of the new channel. MUST be between 2-100 characters long.
	 * @return The new channel.
	 *
	 * @throws DiscordException
	 * @deprecated Use {@link IGuild#createChannel(String)}
	 */
	@Deprecated
	IChannel createChannel(IGuild guild, String name) throws DiscordException, HTTP403Exception;
	
	/**
	 * Gets the regions available for discord.
	 * 
	 * @return The list of available regions.
	 * 
	 * @throws HTTP403Exception
	 */
	List<IRegion> getRegions() throws HTTP403Exception;
	
	/**
	 * Gets the corresponding region for a given id.
	 * 
	 * @param regionID The region id.
	 * @return The region (or null if not found).
	 */
	IRegion getRegionForID(String regionID);
	
	/**
	 * Creates a new guild.
	 * 
	 * @param name The name of the guild.
	 * @param regionID The region id for the guild (defaults to us-west).
	 * @param icon The icon for the guild.
	 * @return The new guild's id.
	 * 
	 * @throws HTTP403Exception
	 */
	IGuild createGuild(String name, Optional<String> regionID, Optional<Image> icon) throws HTTP403Exception;
	
	/**
	 * Represents an avatar image.
	 */
	@FunctionalInterface
	interface Image {
		
		/**
		 * Gets the data to send to discord.
		 *
		 * @return The data to send to discord, can be null.
		 */
		String getData();
		
		/**
		 * Gets the image data (avatar id) for for a user's avatar.
		 *
		 * @param user The user to get the avatar id for.
		 * @return The user's avatar image.
		 */
		static Image forUser(IUser user) {
			return user::getAvatar;
		}
		
		/**
		 * Gets the data (null) for the default discord avatar.
		 *
		 * @return The default avatar image.
		 */
		static Image defaultAvatar() {
			return ()->null;
		}
		
		/**
		 * Generates an avatar image from bytes representing an image.
		 *
		 * @param imageType The image type, ex. jpeg, png, etc.
		 * @param data The image's bytes.
		 * @return The avatar image.
		 */
		static Image forData(String imageType, byte[] data) {
			return ()->String.format("data:image/%s;base64,%s", imageType, Base64.encodeBase64String(data));
		}
		
		/**
		 * Generates an avatar image from an input stream representing an image.
		 *
		 * @param imageType The image type, ex. jpeg, png, etc.
		 * @param stream The image's input stream.
		 * @return The avatar image.
		 */
		static Image forStream(String imageType, InputStream stream) {
			return ()->{
				try {
					Image image = forData(imageType, IOUtils.toByteArray(stream));
					stream.close();
					return image.getData();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return defaultAvatar().getData();
			};
		}
		
		/**
		 * Generates an avatar image from a direct link to an image.
		 *
		 * @param imageType The image type, ex. jpeg, png, etc.
		 * @param url The direct link to an image.
		 * @return The avatar image.
		 */
		static Image forUrl(String imageType, String url) {
			return ()->{
				try {
					URLConnection urlConnection = new URL(url).openConnection();
					InputStream stream = urlConnection.getInputStream();
					return forStream(imageType, stream).getData();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return defaultAvatar().getData();
			};
		}
		
		/**
		 * Generates an avatar image from a file.
		 *
		 * @param file The image file.
		 * @return The avatar image.
		 */
		static Image forFile(File file) {
			return ()->{
				String imageType = FilenameUtils.getExtension(file.getName());
				try {
					return forStream(imageType, new FileInputStream(file)).getData();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				return defaultAvatar().getData();
			};
		}
	}
}
