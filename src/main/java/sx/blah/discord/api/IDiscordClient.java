package sx.blah.discord.api;

import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.modules.ModuleLoader;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.Image;

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
	 * Gets the list of shards this client manages.
	 * @return The shards.
	 */
	List<IShard> getShards();

	/**
	 * Gets the total number of shards this client manages. Purely for convenience.
	 * @return The shard count.
	 */
	int getShardCount();

	/**
	 * Gets the authorization token for this client.
	 *
	 * @return The authorization token.
	 */
	String getToken();

	/**
	 * Logs in every shard this client manages.
	 *
	 * @throws DiscordException This is thrown if there is an error logging in.
	 */
	void login() throws DiscordException, RateLimitException;

	/**
	 * Logs out every shard this client manages.
	 *
	 * @throws DiscordException
	 */
	void logout() throws DiscordException;

	/**
	 * Changes this client's account's username.
	 *
	 * @param username The new username.
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	void changeUsername(String username) throws DiscordException, RateLimitException;

	/**
	 * Changes this client's account's avatar.
	 *
	 * @param avatar The new avatar.
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	void changeAvatar(Image avatar) throws DiscordException, RateLimitException;

	/**
	 * Changes this user's presence on all shards.
	 *
	 * @param isIdle If true, this user becomes idle, or online if false.
	 */
	void changePresence(boolean isIdle);

	/**
	 * Changes the status of the bot user on all shards.
	 *
	 * @param status The new status to use.
	 */
	void changeStatus(Status status);

	/**
	 * Checks if the api is ready to be interacted with on all shards.
	 * @see IShard#isReady()
	 *
	 * @return True if ready, false if otherwise.
	 */
	boolean isReady();

	/**
	 * Checks if the api has established a connection with the Discord gateway on all shards.
	 * @see IShard#isLoggedIn()
	 *
	 * @return True if logged in, false if otherwise.
	 */
	boolean isLoggedIn();

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
	 * @param code The invite code.
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
	 * Gets the connected voice channels.
	 *
	 * @return The voice channels.
	 */
	List<IVoiceChannel> getConnectedVoiceChannels();

	/**
	 * Gets the application description for this bot.
	 *
	 * @return The application's description.
	 *
	 * @throws DiscordException
	 */
	String getApplicationDescription() throws DiscordException;

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
