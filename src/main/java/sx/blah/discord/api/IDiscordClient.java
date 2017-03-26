/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.api;

import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.modules.ModuleLoader;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;

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
	 * @deprecated Use {@link #online()} or {@link #idle()}
	 */
	@Deprecated
	void changePresence(boolean isIdle);

	/**
	 * Changes the status of the bot user on all shards.
	 *
	 * @param status The new status to use.
	 * @deprecated Use {@link #streaming(String, String)} or the online/idle methods with the playing text
	 */
	@Deprecated
	void changeStatus(Status status);

	/**
	 * Set this user/all shards' presence to have this playing text while retaining the status. Can be null.
	 *
	 * @param playingText The (nullable) playing text
	 */
	void changePlayingText(String playingText);

	/**
	 * Set this user/all shards' presences to be online, with the playing text.
	 *
	 * @param playingText The game playing text
	 */
	void online(String playingText);

	/**
	 * Set this user/all shards' presences to be online, retaining the original playing text (if any).
	 */
	void online();

	/**
	 * Set this user/all shards' presences to be idle, with the playing text.
	 *
	 * @param playingText The game playing text
	 */
	void idle(String playingText);

	/**
	 * Set this user/all shards' presences to be idle, retaining the original playing text (if any).
	 */
	void idle();

	/**
	 * Set this user/all shards' presences to be streaming, using the provided game playing text and streaming URL.
	 *
	 * @param playingText The game playing text
	 * @param streamingUrl The streaming URL (Twitch, YouTube Gaming, etc.)
	 */
	void streaming(String playingText, String streamingUrl);

	/**
	 * Checks if the api is ready to be interacted with on all shards.
	 * @see IShard#isReady()
	 *
	 * @return True if ready, false if otherwise.
	 */
	boolean isReady();

	/**
	 * Ensures the client is ready to interact with the api.
	 *
	 * @param action The action that is being attempted. (i.e. "send message")
	 * @throws DiscordException
	 */
	default void checkReady(String action) throws DiscordException {
		if (!isReady()) throw new DiscordException("Attempt to " + action + " before client is ready!");
	}

	/**
	 * Checks if the client has established a connection with the Discord gateway on all shards.
	 * @see IShard#isLoggedIn()
	 *
	 * @return True if logged in, false if otherwise.
	 */
	boolean isLoggedIn();

	/**
	 * Ensures that the client has established a connection with the Discord gateway on all shards.
	 *
	 * @param action The action that is being attempted. (i.e. "send message")
	 * @throws DiscordException
	 */
	default void checkLoggedIn(String action) throws DiscordException {
		if (!isLoggedIn()) throw new DiscordException("Attempt to " + action + " before client has logged in!");
	}


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
	 * Gets a set of users by their name, ignoring case.
	 *
	 * @param name The name of the desired user(s).
	 * @return A {@link Collection} of {@link User} objects with the provided name.
	 */
	List<IUser> getUsersByName(String name);

	/**
	 * Gets a set of users by their name.
	 *
	 * @param name The name of the desired user(s).
	 * @param ignoreCase Whether to ignore the case of the user's name.
	 * @return A {@link Collection} of {@link User} objects with the provided name.
	 */
	List<IUser> getUsersByName(String name, boolean ignoreCase);

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
	IInvite getInviteForCode(String code) throws DiscordException, RateLimitException;

	/**
	 * Gets the regions available for discord.
	 *
	 * @return The list of available regions.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	List<IRegion> getRegions() throws DiscordException, RateLimitException;

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
