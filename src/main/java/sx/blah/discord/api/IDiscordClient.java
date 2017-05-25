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
	void login();

	/**
	 * Logs out every shard this client manages.
	 *
	 * @throws DiscordException
	 */
	void logout();

	/**
	 * Changes this client's account's username.
	 *
	 * @param username The new username.
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	void changeUsername(String username);

	/**
	 * Changes this client's account's avatar.
	 *
	 * @param avatar The new avatar.
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	void changeAvatar(Image avatar);

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
	 * @param streamingUrl The streaming URL (Twitch)
	 */
	void streaming(String playingText, String streamingUrl);

	/**
	 * Changes this user's self-muted state in a guild.
	 *
	 * @param guild The guild to mute this user in.
	 * @param isSelfMuted The new self-muted state.
	 */
	void mute(IGuild guild, boolean isSelfMuted);

	/**
	 * Changes this user's self-deafened state in a guild.
	 *
	 * @param guild The guild to deafen this user in.
	 * @param isSelfDeafened The new self-deafened state.
	 */
	void deafen(IGuild guild, boolean isSelfDeafened);

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
	default void checkReady(String action) {
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
	default void checkLoggedIn(String action) {
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
	 * @deprecated Use {@link #getChannelByID(long)} instead
	 */
	@Deprecated
	default IChannel getChannelByID(String channelID) {
		if (channelID == null) return null;
		return getChannelByID(Long.parseUnsignedLong(channelID));
	}

	/**
	 * Gets a channel by its unique id.
	 *
	 * @param channelID The id of the desired channel.
	 * @return The {@link Channel} object with the provided id.
	 */
	IChannel getChannelByID(long channelID);

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
	 * @deprecated Use {@link #getVoiceChannelByID(long)} instead
	 */
	@Deprecated
	default IVoiceChannel getVoiceChannelByID(String id) {
		if (id == null) return null;
		return getVoiceChannelByID(Long.parseUnsignedLong(id));
	}

	/**
	 * Gets a voice channel from a given id.
	 *
	 * @param id The voice channel id.
	 * @return The voice channel (or null if not found).
	 */
	IVoiceChannel getVoiceChannelByID(long id);

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
	 * @deprecated Use {@link #getGuildByID(long)} instead
	 */
	@Deprecated
	default IGuild getGuildByID(String guildID) {
		if (guildID == null) return null;
		return getGuildByID(Long.parseUnsignedLong(guildID));
	}

	/**
	 * Gets a guild by its unique id.
	 *
	 * @param guildID The id of the desired guild.
	 * @return The {@link Guild} object with the provided id.
	 */
	IGuild getGuildByID(long guildID);

	/**
	 * Gets a set of all users visible to the bot user.
	 *
	 * @return A {@link Collection} of all {@link User} objects.
	 */
	List<IUser> getUsers();

	/**
	 * Gets a cached user by its unique id.
	 *
	 * @param userID The id of the desired user.
	 * @return The {@link User} object with the provided id.
	 *
	 * @see #fetchUser(String)
	 * @deprecated Use {@link #getUserByID(long)}
	 */
	@Deprecated
	default IUser getUserByID(String userID) {
		if (userID == null) return null;
		return getUserByID(Long.parseUnsignedLong(userID));
	}

	/**
	 * Gets a cached user by its unique id.
	 *
	 * @param userID The id of the desired user.
	 * @return The {@link User} object with the provided id.
	 *
	 * @see #fetchUser(long)
	 */
	IUser getUserByID(long userID);

	/**
	 * This attempts to retrieve a user by its id by first checking the api's internal cache, if the user is not found,
	 * a REST request is performed.
	 *
	 * @param id The id of the user to find.
	 * @return Ths user fetched, if found.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 *
	 * @see #getUserByID(String)
	 * @deprecated Use {@link #fetchUser(long)} instead
	 */
	@Deprecated
	default IUser fetchUser(String id) {
		if (id == null) return null;
		return fetchUser(Long.parseUnsignedLong(id));
	}

	/**
	 * This attempts to retrieve a user by its id by first checking the api's internal cache, if the user is not found,
	 * a REST request is performed.
	 *
	 * @param id The id of the user to find.
	 * @return Ths user fetched, if found.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 *
	 * @see #getUserByID(long)
	 */
	IUser fetchUser(long id);

	/**
	 * Gets a set of users by their name, case-sensitive.
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
	 * @deprecated Use {@link #getRoleByID(long)} instead
	 */
	@Deprecated
	default IRole getRoleByID(String roleID) {
		if (roleID == null) return null;
		return getRoleByID(Long.parseUnsignedLong(roleID));
	}

	/**
	 * Gets a role by its unique id.
	 *
	 * @param roleID The id of the desired role.
	 * @return The {@link Role} object
	 */
	IRole getRoleByID(long roleID);

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
	 * <b>NOTE:</b> This only checks already cached messages, see {@link IChannel#getMessageByID(String)} if you want
	 * to retrieve messages which aren't cached already.
	 *
	 * @param messageID The message id of the message to find.
	 * @return The message or null if not found.
	 * @deprecated Use {@link #getMessageByID(long)} instead
	 */
	@Deprecated
	default IMessage getMessageByID(String messageID) {
		if (messageID == null) return null;
		return getMessageByID(Long.parseUnsignedLong(messageID));
	}

	/**
	 * This attempts to search all guilds/private channels for a message.
	 * <b>NOTE:</b> This only checks already cached messages, see {@link IChannel#getMessageByID(long)} if you want
	 * to retrieve messages which aren't cached already.
	 *
	 * @param messageID The message id of the message to find.
	 * @return The message or null if not found.
	 */
	IMessage getMessageByID(long messageID);

	/**
	 * Gets a {@link IPrivateChannel} for the provided recipient.
	 *
	 * @param user The user who will be the recipient of the private channel.
	 * @return The {@link IPrivateChannel} object.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	IPrivateChannel getOrCreatePMChannel(IUser user);

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
	List<IRegion> getRegions();

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
	String getApplicationDescription();

	/**
	 * Gets the url leading to this bot's application's icon.
	 *
	 * @return The application's icon url.
	 *
	 * @throws DiscordException
	 */
	String getApplicationIconURL();

	/**
	 * Gets the bot's application's client id.
	 *
	 * @return The application's client id.
	 *
	 * @throws DiscordException
	 */
	String getApplicationClientID();

	/**
	 * Gets the bot's application's name.
	 *
	 * @return The application's name.
	 *
	 * @throws DiscordException
	 */
	String getApplicationName();

	/**
	 * Gets the bot's application's owner.
	 *
	 * @return The application's owner.
	 *
	 * @throws DiscordException
	 */
	IUser getApplicationOwner();
}
