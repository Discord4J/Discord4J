package sx.blah.discord.api;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.modules.ModuleLoader;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.Image;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Represents the main discord api.
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
	 * @throws RateLimitException
	 */
	void logout() throws RateLimitException, DiscordException;

	/**
	 * Changes this client's account's username.
	 *
	 * @param username The new username.
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	void changeUsername(String username) throws DiscordException, RateLimitException;

	/**
	 * Changes this client's account's email.
	 *
	 * @param email The new email.
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	void changeEmail(String email) throws DiscordException, RateLimitException;

	/**
	 * Changes this client's account's password.
	 *
	 * @param password The new password.
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	void changePassword(String password) throws DiscordException, RateLimitException;

	/**
	 * Changes this client's account's avatar.
	 *
	 * @param avatar The new avatar.
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	void changeAvatar(Image avatar) throws DiscordException, RateLimitException;

	/**
	 * Updates the bot's presence.
	 *
	 * @param isIdle If true, the bot will be "idle", otherwise the bot will be "online".
	 * @param game The optional name of the game the bot is playing. If empty, the bot simply won't be playing a game.
	 * @deprecated Use {@link #changePresence(boolean)} or {@link #changeStatus(Status)}
	 */
	@Deprecated
	void updatePresence(boolean isIdle, Optional<String> game);

	/**
	 * Changes this user's presence.
	 *
	 * @param isIdle If true, this user becomes idle, or online if false.
	 */
	void changePresence(boolean isIdle);

	/**
	 * Changes the game status message for this bot's user.
	 *
	 * @param game The game, or if null then no message will be shown.
	 * @deprecated Use {@link #changeStatus(Status)} instead.
	 */
	@Deprecated
	void changeGameStatus(String game);

	/**
	 * Changes the status of the bot user.
	 *
	 * @param status The new status to use.
	 */
	void changeStatus(Status status);

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
	 * Gets a set of all channels visible to the bot user.
	 *
	 * @param includePrivate Whether to include private channels in the set.
	 * @return A {@link Collection} of all {@link Channel} objects.
	 */
	Collection<IChannel> getChannels(boolean includePrivate);

	/**
	 * Gets a channel by its unique id.
	 *
	 * @param channelID The id of the desired channel.
	 * @return The {@link Channel} object with the provided id.
	 */
	IChannel getChannelByID(String channelID);

	/**
	 * Gets a set of all voice channels visible to the bot user.
	 *
	 * @return A {@link Collection} of all {@link VoiceChannel} objects.
	 */
	Collection<IVoiceChannel> getVoiceChannels();

	/**
	 * Gets a voice channel from a given id.
	 *
	 * @param id The voice channel id.
	 * @return The voice channel (or null if not found).
	 */
	IVoiceChannel getVoiceChannelByID(String id);

	/**
	 * Gets all the guilds the user the api represents is connected to.
	 *
	 * @return The list of {@link Guild}s the api is connected to.
	 */
	List<IGuild> getGuilds();

	/**
	 * Gets a guild by its unique id.
	 *
	 * @param guildID The id of the desired guild.
	 * @return The {@link Guild} object with the provided id.
	 */
	IGuild getGuildByID(String guildID);

	/**
	 * Gets a set of all users visible to the bot user.
	 *
	 * @return A {@link Collection} of all {@link User} objects.
	 */
	Collection<IUser> getUsers();

	/**
	 * Gets a user by its unique id.
	 *
	 * @param userID The id of the desired user.
	 * @return The {@link User} object with the provided id.
	 */
	IUser getUserByID(String userID);

	/**
	 * Gets a set of all roles visible to the bot user.
	 *
	 * @return A {@link Collection} of all {@link Role} objects.
	 */
	Collection<IRole> getRoles();

	/**
	 * Gets a role by its unique id.
	 *
	 * @param roleID The id of the desired role.
	 * @return The {@link Role} object
	 */
	IRole getRoleByID(String roleID);

	/**
	 * Gets a {@link PrivateChannel} for the provided recipient.
	 *
	 * @param user The user who will be the recipient of the private channel.
	 * @return The {@link PrivateChannel} object.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	IPrivateChannel getOrCreatePMChannel(IUser user) throws DiscordException, RateLimitException;

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
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	List<IRegion> getRegions() throws RateLimitException, DiscordException;

	/**
	 * Gets the corresponding region for a given id.
	 *
	 * @param regionID The region id.
	 * @return The region (or null if not found).
	 */
	IRegion getRegionByID(String regionID);

	/**
	 * Creates a new guild.
	 *
	 * @param name The name of the guild.
	 * @param region The region for the guild.
	 * @param icon The icon for the guild.
	 * @return The new guild's id.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @deprecated Use {@link #createGuild(String, IRegion, Image)} or {@link #createGuild(String, IRegion)} instead.
	 */
	@Deprecated
	IGuild createGuild(String name, IRegion region, Optional<Image> icon) throws RateLimitException, DiscordException;

	/**
	 * Creates a new guild.
	 *
	 * @param name The name of the guild.
	 * @param region The region for the guild.
	 * @return The new guild's id.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	IGuild createGuild(String name, IRegion region) throws RateLimitException, DiscordException;

	/**
	 * Creates a new guild.
	 *
	 * @param name The name of the guild.
	 * @param region The region for the guild.
	 * @param icon The icon for the guild.
	 * @return The new guild's id.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	IGuild createGuild(String name, IRegion region, Image icon) throws RateLimitException, DiscordException;

	/**
	 * Gets the latest response time by the discord websocket to a ping.
	 *
	 * @return The response time (in ms).
	 */
	long getResponseTime();

	/**
	 * Gets the connected voice channels.
	 *
	 * @return The voice channels.
	 */
	List<IVoiceChannel> getConnectedVoiceChannels();

	/**
	 * Gets whether or not this client represents a bot account.
	 *
	 * @return True if a bot, false if otherwise.
	 */
	boolean isBot();
	
	/**
	 * Gets the current shard this client is running on.
	 *
	 * @return The current shard id.
	 */
	int getCurrentShard();
	
	/**
	 * Gets the number of shards this account is spread across.
	 *
	 * @return The shard count.
	 */
	int getShardCount();
	
	/**
	 * Gets the applications owned by this user.
	 *
	 * @return The list of owned applications.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	List<IApplication> getApplications() throws RateLimitException, DiscordException;

	/**
	 * Creates a new application for this user.
	 *
	 * @param name The name of the application.
	 * @return The application object.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	IApplication createApplication(String name) throws DiscordException, RateLimitException;

	/**
	 * Gets the time when this client was last logged into. Useful for keeping track of uptime.
	 * Note: See {@link Discord4J#getLaunchTime()} for uptime of the bot application as a whole.
	 *
	 * @return The launch time.
	 */
	LocalDateTime getLaunchTime();

	/**
	 * Gets the application description for this bot.
	 *
	 * @return The application's description.
	 *
	 * @throws DiscordException
	 */
	String getDescription() throws DiscordException;

	/**
	 * Gets the url leading to this bot's application's icon.
	 *
	 * @return The application's icon url.
	 *
	 * @throws DiscordException
	 */
	String getApplicationIconURL() throws DiscordException;

	/**
	 * Gets the bot's application's client id.
	 *
	 * @return The application's client id.
	 *
	 * @throws DiscordException
	 */
	String getApplicationClientID() throws DiscordException;

	/**
	 * Gets the bot's application's name.
	 *
	 * @return The application's name.
	 *
	 * @throws DiscordException
	 */
	String getApplicationName() throws DiscordException;
}
