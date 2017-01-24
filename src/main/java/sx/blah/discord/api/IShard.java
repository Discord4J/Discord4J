package sx.blah.discord.api;

import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.impl.obj.Role;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.impl.obj.VoiceChannel;
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
	void login() throws DiscordException;

	/**
	 * Disconnects this shard from the Discord gateway.
	 * @throws DiscordException
	 */
	void logout() throws DiscordException;

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
	default void checkReady(String action) throws DiscordException {
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
	default void checkLoggedIn(String action) throws DiscordException {
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
	 * @param streamingUrl The streaming URL (Twitch, YouTube Gaming, etc.)
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
	 */
	IChannel getChannelByID(String channelID);

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
	 */
	IVoiceChannel getVoiceChannelByID(String id);

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
	 */
	IGuild getGuildByID(String guildID);

	/**
	 * Gets a set of all users visible to this shard.
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
	 */
	IRole getRoleByID(String roleID);

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
}
