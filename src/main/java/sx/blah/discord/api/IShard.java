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

import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;

import java.util.List;

/**
 * A single shard connection to Discord. Each shard has its own gateway connection
 * ({@link sx.blah.discord.api.internal.DiscordWS} instance) and is only associated with other shards by their parent
 * {@link IDiscordClient}.
 *
 * @see <a href=https://discordapp.com/developers/docs/topics/gateway#sharding>Sharding</a>
 */
public interface IShard {

	/**
	 * Gets the client that manages this shard.
	 *
	 * @return The client that manages this shard.
	 */
	IDiscordClient getClient();

	/**
	 * Gets the sharding information for this shard. Index 0 is the current shard number and index 1 is the total number of shards.
	 *
	 * @return The sharding information.
	 */
	int[] getInfo();

	/**
	 * Begins the connection process to the Discord gateway.
	 */
	void login();

	/**
	 * Disconnects this shard from the Discord gateway.
	 */
	void logout();

	/**
	 * Gets whether the the shard is fully logged in. This means all available guilds have been received on this shard.
	 * All functionality of the shard is not available until this is true.
	 *
	 * @return Whether the shard is fully logged in.
	 */
	boolean isReady();

	/**
	 * Throws an exception if the shard is not ready.
	 *
	 * @param action The action that is being attempted. (i.e. "send message")
	 * @see #isReady()
	 */
	default void checkReady(String action) {
		if (!isReady()) throw new DiscordException("Attempt to " + action + " before shard is ready!");
	}

	/**
	 * Gets whether the shard has received the READY payload on the gateway.
	 *
	 * <p>This is <b>not the same</b> as {@link #isReady()}. A small subset of the functionality of the shard is
	 * available if this is true, but not all.
	 *
	 * @return Whether every shard has begun its connection process with Discord.
	 */
	boolean isLoggedIn();

	/**
	 * Throws an exception if the shard is not logged in.
	 *
	 * @param action The action that is being attempted. (i.e. "send message")
	 * @see #isLoggedIn()
	 */
	default void checkLoggedIn(String action) {
		if (!isLoggedIn()) throw new DiscordException("Attempt to " + action + " before shard has logged in!");
	}

	/**
	 * Gets the last time it took for Discord to acknowledge a heartbeat.
	 *
	 * @return The last time it took for Discord to acknowledge a heartbeat.
	 */
	long getResponseTime();

	/**
	 * Changes the playing text of the bot on the shard. The previously-set online status will be maintained.
	 *
	 * <p>Note: Due to the fact that Discord doesn't send updates for the bot user's presence, it is possible that using
	 * this method will overwrite an online status set by a different gateway connection.
	 *
	 * @param playingText The nullable playing text.
	 */
	void changePlayingText(String playingText);

	/**
	 * Changes the online status of the bot to online with the given playing text on the shard.
	 *
	 * @param playingText The nullable playing text.
	 */
	void online(String playingText);

	/**
	 * Changes the online status of the bot to online on the shard.
	 */
	void online();

	/**
	 * Changes the online status of the bot to idle with the given playing text on the shard.
	 *
	 * @param playingText The nullable playing text.
	 */
	void idle(String playingText);

	/**
	 * Changes the online status of the bot to idle on the shard.
	 */
	void idle();

	/**
	 * Changes the online status of the bot to streaming with the given playing text and stream url on the shard.
	 *
	 * @param playingText The nullable playing text.
	 * @param streamingUrl The valid twitch.tv streaming url.
	 */
	void streaming(String playingText, String streamingUrl);

	/**
	 * Changes the online status of the bot to do not disturb with the given playing text on the shard.
	 *
	 * @param playingText The nullable playing text.
	 */
	void dnd(String playingText);

	/**
	 * Changes the online status of the bot to do not disturb on the shard.
	 */
	void dnd();

	/**
	 * Changes the online status of the bot to invisible on the shard.
	 */
	void invisible();

	/**
	 * Gets a list of all text channels visible to the bot user on the shard.
	 *
	 * @param includePrivate Whether to include private channels.
	 * @return A list of all visible text channels.
	 */
	List<IChannel> getChannels(boolean includePrivate);

	/**
	 * Gets a list of all non-private text channels visible to the bot user on the shard.
	 *
	 * <p>This is equivalent to <code>getChannels(false)</code>.
	 *
	 * @return A list of all visible, non-private text channels.
	 */
	List<IChannel> getChannels();

	/**
	 * Gets a text channel by its unique snowflake ID from the shard's text channel cache.
	 *
	 * @param channelID The ID of the desired channel.
	 * @return The text channel with the provided ID (or null if one was not found).
	 */
	IChannel getChannelByID(long channelID);

	/**
	 * Gets a list of all voice channels visible to the bot user on the shard.
	 *
	 * @return A list of all visible voice channels.
	 */
	List<IVoiceChannel> getVoiceChannels();

	/**
	 * Gets a list of voice channels the client is connected to on the shard.
	 *
	 * <p>A bot may be connected to one voice channel per guild.
	 *
	 * @return A list of connected voice channels.
	 */
	List<IVoiceChannel> getConnectedVoiceChannels();

	/**
	 * Gets a voice channel by its unique snowflake ID from the shard's voice channel cache.
	 *
	 * @param id The ID of the desired channel.
	 * @return The voice channel with the provided ID (or null if one was not found).
	 */
	IVoiceChannel getVoiceChannelByID(long id);

	/**
	 * Gets a list of the guilds the bot user is a member of that the shard received.
	 *
	 * @return A list of guilds the bot user is a member of that the shard received.
	 */
	List<IGuild> getGuilds();

	/**
	 * Gets a guild by its unique snowflake ID from the shard's guild cache.
	 *
	 * @param guildID The ID of the desired guild.
	 * @return The guild with the provided ID (or null if one was not found).
	 */
	IGuild getGuildByID(long guildID);

	/**
	 * Gets a list of all users visible to the bot user on the shard.
	 *
	 * <p>Note: This list <b>does</b> contain the bot user.
	 *
	 * @return A list of all visible users.
	 */
	List<IUser> getUsers();

	/**
	 * Gets a user by its unique snowflake ID from the shard's user cache.
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
	 * Gets a user by its unique snowflake ID from the shard's user cache <b>or</b> by fetching it from Discord.
	 *
	 * <p>Discord allows the fetching of users the bot cannot directly see (they share no mutual guilds, so they are not
	 * cached). This method first checks the client's user cache and if there is no such user with the provided ID, it
	 * is requested from Discord.
	 *
	 * <p>Use {@link #getUserByID(long)} to only search the shard's user cache.
	 *
	 * @return The user with the provided ID (nor null if one was not found).
	 */
	IUser fetchUser(long id);

	/**
	 * Gets a list of all roles visible to the bot user on the shard.
	 *
	 * @return A list of all visible roles.
	 */
	List<IRole> getRoles();

	/**
	 * Gets a role by its unique snowflake ID from the shard's role cache.
	 *
	 * @param roleID The ID of the desired role.
	 * @return The role with the provided ID (or null if one was not found).
	 */
	IRole getRoleByID(long roleID);

	/**
	 * Gets a list of all messages in the shard's message cache.
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
	 * Gets a message by its unique snowflake ID from the shard's message cache.
	 *
	 * @param messageID The ID of the desired role.
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
	 * Gets a list of all categories visible to the bot user on the shard.
	 *
	 * @return A list of all visible categories.
	 */
	List<ICategory> getCategories();

	/**
	 * Gets a category by its unique snowflake ID from the shard's category cache.
	 *
	 * @param categoryID The ID of the desired category.
	 * @return The category with the provided ID (or null if one was not found).
	 */
	ICategory getCategoryByID(long categoryID);
}
