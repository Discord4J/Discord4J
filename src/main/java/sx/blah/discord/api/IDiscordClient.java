package sx.blah.discord.api;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import sx.blah.discord.handle.EventDispatcher;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.modules.ModuleLoader;
import sx.blah.discord.util.HTTP429Exception;

import java.io.*;
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
	 * Gets the {@link ModuleLoader} instance for this client.
	 * 
	 * @return The module loader instance.
	 */
	ModuleLoader getModuleLoader();
	
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
	 * @throws HTTP429Exception
	 */
	void logout() throws HTTP429Exception;
	
	/**
	 * Allows you to change the info on your bot.
	 *
	 * @param username Username (if you want to change it).
	 * @param email Email (if you want to change it)
	 * @param password Password (if you want to change it).
	 * @param avatar Image data for the bot's avatar, {@link Image}
	 *
	 * @throws HTTP429Exception
	 */
	void changeAccountInfo(Optional<String> username, Optional<String> email, Optional<String> password, Optional<Image> avatar) throws HTTP429Exception; 
	
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
	 * Gets the invite for a code.
	 *
	 * @param code The invite code or xkcd pass.
	 * @return The invite, or null if it doesn't exist.
	 */
	IInvite getInviteForCode(String code);
	
	/**
	 * Gets the regions available for discord.
	 * 
	 * @return The list of available regions.
	 * 
	 * @throws HTTP429Exception
	 */
	List<IRegion> getRegions() throws HTTP429Exception;
	
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
	 * @throws HTTP429Exception
	 */
	IGuild createGuild(String name, Optional<String> regionID, Optional<Image> icon) throws HTTP429Exception;
	
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
