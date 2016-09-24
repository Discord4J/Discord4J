package sx.blah.discord.api;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.modules.ModuleLoader;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.Image;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

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
	 * @param async Whether to wait for all guilds before dispatching the {@link sx.blah.discord.handle.impl.events.ReadyEvent}.
	 *
	 * @throws DiscordException This is thrown if there is an error logging in.
	 */
	void login(boolean async) throws DiscordException;

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
	 * Changes this user's presence.
	 *
	 * @param isIdle If true, this user becomes idle, or online if false.
	 */
	void changePresence(boolean isIdle);

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
	 * Checks if the websocket is ready to be interacted with
	 *
	 * @param shard The shard of the websocket being check
	 *
	 * @return True if the the shard's websocket is ready, false if otherwise
     */
	boolean isReady(int shard);
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
	List<IChannel> getChannels(boolean includePrivate);

	/**
	 * Gets a set of all channels visible to the bot user.
	 *
	 * @return A {@link Collection} of all non-private {@link Channel} objects.
	 */
	List<IChannel> getChannels();

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
	List<IVoiceChannel> getVoiceChannels();

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
	 * Gets all guilds the api is connected to on a certain shard
	 *
	 * @param shard The shard of the guild list being collected
	 *
	 * @return The list of {@link Guild}s the api is connected to with the certain shard.
     */
	List<IGuild> getGuilds(int shard);

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
	List<IUser> getUsers();

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
	List<IRole> getRoles();

	/**
	 * Gets a role by its unique id.
	 *
	 * @param roleID The id of the desired role.
	 * @return The {@link Role} object
	 */
	IRole getRoleByID(String roleID);

	/**
	 * This gets all messages stored internally by the bot.
	 *
	 * @param includePrivate Whether to include private messages or not.
	 * @return A collection of all messages.
	 */
	List<IMessage> getMessages(boolean includePrivate);

	/**
	 * This gets all messages stored internally by the bot (including from private channels).
	 *
	 * @return A collection of all messages.
	 */
	List<IMessage> getMessages();

	/**
	 * This attempts to search all guilds/private channels for a message.
	 *
	 * @param messageID The message id of the message to find.
	 * @return The message or null if not found.
	 */
	IMessage getMessageByID(String messageID);

	/**
	 * Gets a {@link IPrivateChannel} for the provided recipient.
	 *
	 * @param user The user who will be the recipient of the private channel.
	 * @return The {@link IPrivateChannel} object.
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
	 * Gets the latest response time by the Discord websocket from a ping.
	 *
	 * @param shard The shard's websocket you are getting the response time of.
	 *
	 * @return The response time (in ms).
	 */
	long getResponseTime(int shard);

	/**
	 * Gets the latest response time by the Discord websocket (shard 0) from a ping.
	 *
	 * @return The response time (in ms).
	 */
	long getResponseTime();

	/**
	 * Gets the average response time by all shards from a ping.
	 *
	 * @return The average response time (in ms).
	 */
	long getAverageResponseTime();

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
	 * Gets the number of shards this account is spread across.
	 *
	 * @return The shard count.
	 */
	int getShardCount();

	/**
	 * Connects the api to a websocket with a shard and gateway
	 *
	 * @param shard The shard of the connecting webscoket.
	 * @param gatewayURL The url the websocket is connecting to.
     */
	void connectWebSocket(int shard, String gatewayURL);

	/**
	 * Disconnects the websocket from te api
	 *
	 * @param shard The shard of the disconnecting webscoket
	 * @param reason The reason why the websocket is being disconnected
	 */
	void disconnectWebSocket(int shard, DiscordDisconnectedEvent.Reason reason);

	/**
	 * Change the amount of shards the websockets are using
	 *
	 * @param shardCount The new amount of shards the guilds should span across
     */
	void setShardCount(int shardCount);
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

	/**
	 * Gets the bot's application's owner.
	 *
	 * @return The application's owner.
	 *
	 * @throws DiscordException
	 */
	IUser getApplicationOwner() throws DiscordException;
}
