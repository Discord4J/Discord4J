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
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.modules.ModuleLoader;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;

import java.util.List;

/**
 * The main method by which interaction with Discord is done.
 *
 * <p>This represents a "client" which can manage multiple {@link IShard} instances. Most methods which interact
 * directly with Discord simply execute the equivalent method in all of the managed {@link IShard}s.
 */
public interface IDiscordClient {

	/**
	 * Gets the {@link EventDispatcher} responsible for managing events for the client.
	 *
	 * @return The event dispatcher for the client.
	 */
	EventDispatcher getDispatcher();

	/**
	 * Gets the {@link ModuleLoader} responsible for loading modules for the client.
	 *
	 * @return The module loader for the client.
	 */
	ModuleLoader getModuleLoader();

	/**
	 * Gets the shards which the client manages. After login, this is always at least of size <code>1</code>.
	 *
	 * @return The shards managed by the client.
	 */
	List<IShard> getShards();

	/**
	 * Gets the total number of shards the client manages. This is not necessarily the same as
	 * <code>getShards().size()</code> because shards are not created and added to the list until login.
	 *
	 * @return The total number of shards the client manages.
	 */
	int getShardCount();

	/**
	 * Gets the authentication token for the client.
	 *
	 * @return The authentication token for the client.
	 */
	String getToken();

	/**
	 * Begins the login process for every managed shard.
	 */
	void login();

	/**
	 * Gracefully disconnects every managed shard.
	 *
	 * <p>This will automatically attempt to disconnect from any connected voice channels and cancels any threads
	 * controlled by the client.
	 */
	void logout();

	/**
	 * Makes a request to Discord to change the username of the bot.
	 *
	 * @param username The new username.
	 */
	void changeUsername(String username);

	/**
	 * Makes a request to Discord to change the avatar of the bot.
	 *
	 * @param avatar The new avatar.
	 */
	void changeAvatar(Image avatar);

	/**
	 * Changes the presence of the bot.
	 *
	 * @param status The status to display.
	 * @param activity The type of activity to display.
	 * @param text The text to display.
	 *
	 * @throws IllegalArgumentException If activity is {@link ActivityType#STREAMING}.
	 * Use {@link #changeStreamingPresence(StatusType, String, String)} instead.
	 */
	void changePresence(StatusType status, ActivityType activity, String text);

	/**
	 * Changes the presence of the bot.
	 *
	 * @param type The status to display.
	 */
	void changePresence(StatusType type);

	/**
	 * Changes the presence of the bot to streaming.
	 *
	 * @param status The status to display.
	 * @param text The text to display, may be null.
	 * @param streamUrl The valid twitch.tv streaming url.
	 */
	void changeStreamingPresence(StatusType status, String text, String streamUrl);

	/**
	 * Changes the bot user's self-muted state in a guild.
	 *
	 * @param guild The guild to self mute the bot user in.
	 * @param isSelfMuted The new self-muted state.
	 */
	void mute(IGuild guild, boolean isSelfMuted);

	/**
	 * Changes the bot user's self-deafened state in a guild.
	 *
	 * @param guild The guild to deafen the bot user in.
	 * @param isSelfDeafened The new self-deafened state.
	 */
	void deafen(IGuild guild, boolean isSelfDeafened);

	/**
	 * Gets whether the the client is fully logged in on all shards. All functionality of the client is not available
	 * until this is true.
	 *
	 * @return Whether the client is fully logged in on all shards.
	 */
	boolean isReady();

	/**
	 * Throws an exception if the client is not ready.
	 *
	 * @param action The action that is being attempted. (i.e. "send message")
	 * @see #isReady()
	 */
	default void checkReady(String action) {
		if (!isReady()) throw new DiscordException("Attempt to " + action + " before client is ready!");
	}

	/**
	 * Gets whether every shard has received the READY payload on the gateway.
	 *
	 * <p>This is <b>not the same</b> as {@link #isReady()}. A small subset of the functionality of the client is
	 * available if this is true, but not all.
	 *
	 * @return Whether every shard has begun its connection process with Discord.
	 */
	boolean isLoggedIn();

	/**
	 * Throws an exception if the client is not logged in.
	 *
	 * @param action The action that is being attempted. (i.e. "send message")
	 * @see #isLoggedIn()
	 */
	default void checkLoggedIn(String action) {
		if (!isLoggedIn()) throw new DiscordException("Attempt to " + action + " before client has logged in!");
	}

	/**
	 * Gets the {@link IUser} which represents the client in Discord.
	 *
	 * @return The user which represents the client in Discord.
	 */
	IUser getOurUser();

	/**
	 * Gets a list of all text channels visible to the bot user on every shard.
	 *
	 * @param includePrivate Whether to include private channels.
	 * @return A list of all visible text channels.
	 */
	List<IChannel> getChannels(boolean includePrivate);

	/**
	 * Gets a list of all non-private text channels visible to the bot user on every shard.
	 *
	 * <p>This is equivalent to <code>getChannels(false)</code>.
	 *
	 * @return A list of all visible, non-private text channels.
	 */
	List<IChannel> getChannels();

	/**
	 * Gets a text channel by its unique snowflake ID from the client's text channel cache.
	 *
	 * @param channelID The ID of the desired channel.
	 * @return The text channel with the provided ID (or null if one was not found).
	 */
	IChannel getChannelByID(long channelID);

	/**
	 * Gets a list of all voice channels visible to the bot user on every shard.
	 *
	 * @return A list of all visible voice channels.
	 */
	List<IVoiceChannel> getVoiceChannels();

	/**
	 * Gets a voice channel by its unique snowflake ID from the client's voice channel cache.
	 *
	 * @param id The ID of the desired channel.
	 * @return The voice channel with the provided ID (or null if one was not found).
	 */
	IVoiceChannel getVoiceChannelByID(long id);

	/**
	 * Gets a list of all guilds on every shard the bot user is a member of.
	 *
	 * @return A list of all guilds on every shard the bot user is a member of.
	 */
	List<IGuild> getGuilds();

	/**
	 * Gets a guild by its unique snowflake ID from the client's guild cache.
	 *
	 * @param guildID The ID of the desired guild.
	 * @return The guild with the provided ID (or null if one was not found).
	 */
	IGuild getGuildByID(long guildID);

	/**
	 * Gets a list of all users visible to the bot user on every shard.
	 *
	 * <p>Note: This list <b>does</b> contain the bot user.
	 *
	 * @return A list of all visible users.
	 */
	List<IUser> getUsers();

	/**
	 * Gets a user by its unique snowflake ID from the client's user cache.
	 *
	 * <p>Note: This method only searches the client's list of <b>cached</b> users. Discord allows the fetching of users
	 * which the bot cannot directly see (they share no mutual guilds, so they are not cached). This functionality is
	 * exposed through {@link #fetchUser(long)}.
	 *
	 * @param userID The ID of the desired user.
	 * @return The user with the provided ID (or null if one was not found).
	 */
	IUser getUserByID(long userID);

	/**
	 * Gets a user by its unique snowflake ID from the client's user cache <b>or</b> by fetching it from Discord.
	 *
	 * <p>Discord allows the fetching of users the bot cannot directly see (they share no mutual guilds, so they are not
	 * cached). This method first checks the client's user cache and if there is no such user with the provided ID, it
	 * is requested from Discord.
	 *
	 * <p>Use {@link #getUserByID(long)} to only search the client's user cache.
	 *
	 * @param id The ID of the desired user.
	 * @return The user with the provided ID (or null if one was not found).
	 */
	IUser fetchUser(long id);

	/**
	 * Gets a list of users by their name.
	 *
	 * <p>This is equivalent to <code>getUsersByName(name, false)</code>
	 *
	 * @param name The case-sensitive name of the desired users.
	 * @return A list of users with the provided name.
	 */
	List<IUser> getUsersByName(String name);

	/**
	 * Gets a list of users by their name.
	 *
	 * @param name The name of the desired users.
	 * @param ignoreCase Whether to ignore the case of the user's name.
	 * @return A list of users with the provided name.
	 */
	List<IUser> getUsersByName(String name, boolean ignoreCase);

	/**
	 * Gets a list of all roles visible to the bot user on every shard.
	 *
	 * @return A list of all visible roles.
	 */
	List<IRole> getRoles();

	/**
	 * Gets a role by its unique snowflake ID from the client's role cache.
	 *
	 * @param roleID The ID of the desired role.
	 * @return The role with the provided ID (or null if one was not found).
	 */
	IRole getRoleByID(long roleID);

	/**
	 * Gets a list of all messages in the client's message cache.
	 *
	 * @param includePrivate Whether to include private messages.
	 * @return A list of all cached messages.
	 */
	List<IMessage> getMessages(boolean includePrivate);

	/**
	 * Gets a list of all messages in the client's message cache.
	 *
	 * <p>This is equivalent to <code>getMessages(true)</code>
	 *
	 * @return A list of all cached messages.
	 */
	List<IMessage> getMessages();

	/**
	 * Gets a message by its unique snowflake ID from the client's message cache.
	 *
	 * @param messageID The ID of the desired message.
	 * @return The message with the provided ID (or null if one was not found).
	 */
	IMessage getMessageByID(long messageID);

	/**
	 * Gets the private channel for a user or creates it if one doesn't exist.
	 *
	 * @param user The user to get the private channel for.
	 * @return The private channel for the given user.
	 */
	IPrivateChannel getOrCreatePMChannel(IUser user);

	/**
	 * Makes a request to Discord for an invite with the given unique code.
	 *
	 * @param code The invite code.
	 * @return The invite for the given code (or null if one was not found).
	 */
	IInvite getInviteForCode(String code);

	/**
	 * Gets a list of available voice regions from Discord.
	 *
	 * @return The list of available voice regions.
	 */
	List<IRegion> getRegions();

	/**
	 * Gets a voice region by its unique ID.
	 *
	 * @param regionID The region ID.
	 * @return The region for the given ID (or null if one was not found).
	 */
	IRegion getRegionByID(String regionID);

	/**
	 * Gets a list of voice channels the client is connected to on every shard.
	 *
	 * <p>A bot may be connected to one voice channel per guild.
	 *
	 * @return A list of connected voice channels.
	 */
	List<IVoiceChannel> getConnectedVoiceChannels();

	/**
	 * Gets the bot's associated application's description.
	 *
	 * @return The application's description.
	 * @see <a href=https://discordapp.com/developers/applications/me>Applications</a>
	 */
	String getApplicationDescription();

	/**
	 * Gets the bot's associated application's icon image url.
	 *
	 * @return The application's icon url.
	 * @see <a href=https://discordapp.com/developers/applications/me>Applications</a>
	 */
	String getApplicationIconURL();

	/**
	 * Gets the bot's associated application's client ID.
	 *
	 * @return The application's client ID.
	 * @see <a href=https://discordapp.com/developers/applications/me>Applications</a>
	 */
	String getApplicationClientID();

	/**
	 * Gets the bot's associated application's name.
	 *
	 * @return The application's name.
	 * @see <a href=https://discordapp.com/developers/applications/me>Applications</a>
	 */
	String getApplicationName();

	/**
	 * Gets the bot's associated application's owner.
	 *
	 * @return The application's owner.
	 * @see <a href=https://discordapp.com/developers/applications/me>Applications</a>
	 */
	IUser getApplicationOwner();

	/**
	 * Gets a list of all categories visible to the bot user on every shard.
	 *
	 * @return A list of all visible categories.
	 */
	List<ICategory> getCategories();

	/**
	 * Gets a category by its unique snowflake ID from the client's category cache.
	 *
	 * @param categoryID The ID of the desired category.
	 * @return The category with the provided ID (or null if one was not found).
	 */
	ICategory getCategoryByID(long categoryID);

	/**
	 * Gets a list of categories by their name.
	 *
	 * @param name The name of the desired categories.
	 * @return A list of categories with the provided name.
	 */
	List<ICategory> getCategoriesByName(String name);
}
