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

import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

import java.util.Collection;
import java.util.List;

public interface IShard {

	/**
	 * Gets the client this shard is managed by.
	 * @return The client.
	 */
	IDiscordClient getClient();

	/**
	 * Gets the sharding information for this shard. Index 0 is the current shard number and index 1 is the total number of shards.
	 * @return The sharding information.
	 */
	int[] getInfo();

	/**
	 * Connects this shard to the Discord gateway.
	 * @throws DiscordException
	 */
	void login();

	/**
	 * Disconnects this shard from the Discord gateway.
	 * @throws DiscordException
	 */
	void logout();

	/**
	 * Checks if the api is ready to be interacted with. This means all available guilds on this shard have been received.
	 *
	 * @return True if ready, false if otherwise.
	 */
	boolean isReady();

	/**
	 * Ensures the shard is ready to interact with the api.
	 *
	 * @param action The action that is being attempted. (i.e. "send message")
	 * @throws DiscordException
	 */
	default void checkReady(String action) {
		if (!isReady()) throw new DiscordException("Attempt to " + action + " before shard is ready!");
	}

	/**
	 * Checks if the shard has established a connection with the Discord gateway. This means the Ready payload has been received.
	 * Note: This is most likely only useful to advanced users.
	 *
	 * @return True if logged in, false if otherwise.
	 */
	boolean isLoggedIn();

	/**
	 * Ensures that the shard has established a connection with the Discord gateway.
	 *
	 * @param action The action that is being attempted. (i.e. "send message")
	 * @throws DiscordException
	 */
	default void checkLoggedIn(String action) {
		if (!isLoggedIn()) throw new DiscordException("Attempt to " + action + " before shard has logged in!");
	}

	/**
	 * Gets the last time it took for Discord to acknowledge a heartbeat.
	 *
	 * @return The time it took.
	 */
	long getResponseTime();

	/**
	 * Set this shard's presence to have this playing text while retaining the status. Can be null.
	 *
	 * @param playingText The (nullable) playing text
	 */
	void changePlayingText(String playingText);

	/**
	 * Set this shard's presence to be online, with the playing text.
	 *
	 * @param playingText The game playing text
	 */
	void online(String playingText);

	/**
	 * Set this shard's presence to be online, retaining the original playing text (if any).
	 */
	void online();

	/**
	 * Set this shard's presence to be idle, with the playing text.
	 *
	 * @param playingText The game playing text
	 */
	void idle(String playingText);

	/**
	 * Set this shard's presence to be idle, retaining the original playing text (if any).
	 */
	void idle();

	/**
	 * Set this shard's presence to be streaming, using the provided game playing text and streaming URL.
	 *
	 * @param playingText The game playing text
	 * @param streamingUrl The streaming URL (Twitch)
	 */
	void streaming(String playingText, String streamingUrl);

	// Future presences

//	/**
//	 * Set this shard's presence to be in do not disturb mode, using the provided playing text.
//	 * Note that this doesn't stop events from occurring.
//	 *
//	 * @param playingText The game playing text
//	 */
//	void dnd(String playingText);
//
//	/**
//	 * Set this shard's presence to be in do not disturb mode, using the original playing text (if any).
//	 * Note that this doesn't stop events from occurring.
//	 */
//	void dnd();
//
//	/**
//	 * Set this shard's presence to be invisible (appear offline to others).
//	 */
//	void invisible();

	/**
	 * Changes this user's presence.
	 *
	 * @param isIdle If true, this user becomes idle, or online if false.
	 * @deprecated Use {@link #online()} or {@link #idle()}
	 */
	@Deprecated
	void changePresence(boolean isIdle);

	/**
	 * Changes the status of the bot user.
	 *
	 * @param status The new status to use.
	 * @deprecated Use {@link #streaming(String, String)} or the online/idle methods with the playing text
	 */
	@Deprecated
	void changeStatus(Status status);

	/**
	 * Gets a set of all channels visible to this shard.
	 *
	 * @param includePrivate Whether to include private channels in the set.
	 * @return A {@link Collection} of all {@link Channel} objects.
	 */
	List<IChannel> getChannels(boolean includePrivate);

	/**
	 * Gets a set of all channels visible to this shard.
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
	 * Gets a set of all voice channels visible to this shard.
	 *
	 * @return A {@link Collection} of all {@link VoiceChannel} objects.
	 */
	List<IVoiceChannel> getVoiceChannels();

	/**
	 * Gets the connected voice channels on this shard.
	 *
	 * @return The voice channels.
	 */
	List<IVoiceChannel> getConnectedVoiceChannels();

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
	 * Gets a set of all guilds visible to this shard.
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
	 * Gets a set of all users visible to this shard.
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
	 * @deprecated Use {@link #getUserByID(long)} instead
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
	 * Gets a set of all roles visible to this shard.
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
	 * This gets all messages stored in this shard's cache.
	 *
	 * @param includePrivate Whether to include private messages or not.
	 * @return A collection of all messages.
	 */
	List<IMessage> getMessages(boolean includePrivate);

	/**
	 * This gets all messages stored in this shard's cache. (including from private channels).
	 *
	 * @return A collection of all messages.
	 */
	List<IMessage> getMessages();

	/**
	 * This attempts to search all guilds/private channels visible to this shard for a message.
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
	 * This attempts to search all guilds/private channels visible to this shard for a message.
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
}
