package sx.blah.discord.handle.obj;

import sx.blah.discord.handle.AudioChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MissingPermissionsException;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * This class defines a guild/server/clan/whatever it's called.
 */
public interface IGuild extends IDiscordObject<IGuild> {

	/**
	 * Gets the user id for the owner of this guild.
	 *
	 * @return The owner id.
	 */
	String getOwnerID();

	/**
	 * Gets the user object for the owner of this guild.
	 *
	 * @return The owner.
	 */
	IUser getOwner();

	/**
	 * Gets the icon id for this guild.
	 *
	 * @return The icon id.
	 */
	String getIcon();

	/**
	 * Gets the direct link to the guild's icon.
	 *
	 * @return The icon url.
	 */
	String getIconURL();

	/**
	 * Gets all the channels on the server.
	 *
	 * @return All channels on the server.
	 */
	List<IChannel> getChannels();

	/**
	 * Gets a channel on the guild by a specific channel id.
	 *
	 * @param id The ID of the channel you want to find.
	 * @return The channel with given ID.
	 */
	IChannel getChannelByID(String id);

	/**
	 * Gets all the users connected to the guild.
	 *
	 * @return All users connected to the guild.
	 */
	List<IUser> getUsers();

	/**
	 * Gets a user by its id in the guild.
	 *
	 * @param id ID of the user you want to find.
	 * @return The user with given ID.
	 */
	IUser getUserByID(String id);

	/**
	 * Gets the name of the guild.
	 *
	 * @return The name of the guild
	 */
	String getName();

	/**
	 * Gets the roles contained in this guild.
	 *
	 * @return The list of roles in the guild.
	 */
	List<IRole> getRoles();

	/**
	 * Gets a role object for its unique id.
	 *
	 * @param id The role id of the desired role.
	 * @return The role, or null if not found.
	 */
	IRole getRoleByID(String id);

	/**
	 * Gets the voice channels in this guild.
	 *
	 * @return The voice channels.
	 */
	List<IVoiceChannel> getVoiceChannels();

	/**
	 * Gets a voice channel for a give id.
	 *
	 * @param id The channel id.
	 * @return The voice channel (or null if not found).
	 */
	IVoiceChannel getVoiceChannelByID(String id);

	/**
	 * Gets the channel where afk users are placed.
	 *
	 * @return The voice channel (or null if nonexistant).
	 */
	IVoiceChannel getAFKChannel();

	/**
	 * Gets the timeout (in seconds) before a user is placed in the AFK channel.
	 *
	 * @return The timeout.
	 */
	int getAFKTimeout();

	/**
	 * Creates a new role in this guild.
	 *
	 * @return The new role.
	 *
	 * @throws MissingPermissionsException
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 */
	IRole createRole() throws MissingPermissionsException, HTTP429Exception, DiscordException;

	/**
	 * Retrieves the list of banned users from this guild.
	 *
	 * @return The list of banned users.
	 *
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 */
	List<IUser> getBannedUsers() throws HTTP429Exception, DiscordException;

	/**
	 * Bans a user from this guild.
	 *
	 * @param user The user to ban.
	 *
	 * @throws MissingPermissionsException
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 */
	void banUser(IUser user) throws MissingPermissionsException, HTTP429Exception, DiscordException;

	/**
	 * Bans a user from this guild.
	 *
	 * @param user The user to ban.
	 * @param deleteMessagesForDays The number of days to delete messages from this user for.
	 *
	 * @throws MissingPermissionsException
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 */
	void banUser(IUser user, int deleteMessagesForDays) throws MissingPermissionsException, HTTP429Exception, DiscordException;

	/**
	 * This removes a ban on a user.
	 *
	 * @param userID The user to unban.
	 *
	 * @throws MissingPermissionsException
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 */
	void pardonUser(String userID) throws MissingPermissionsException, HTTP429Exception, DiscordException;

	/**
	 * Kicks a user from the guild.
	 *
	 * @param user The user to kick.
	 *
	 * @throws MissingPermissionsException
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 */
	void kickUser(IUser user) throws MissingPermissionsException, HTTP429Exception, DiscordException;

	/**
	 * Edits the roles a user is a part of.
	 *
	 * @param user The user to edit the roles for.
	 * @param roles The roles for the user to have.
	 *
	 * @throws MissingPermissionsException
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 */
	void editUserRoles(IUser user, IRole[] roles) throws MissingPermissionsException, HTTP429Exception, DiscordException;

	/**
	 * Changes the name of the guild.
	 *
	 * @param name The new name of the guild.
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeName(String name) throws HTTP429Exception, DiscordException, MissingPermissionsException;

	/**
	 * Changes the region of the guild.
	 *
	 * @param region The new region of the guild.
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeRegion(IRegion region) throws HTTP429Exception, DiscordException, MissingPermissionsException;

	/**
	 * Changes the name of the guild.
	 *
	 * @param icon The new icon of the guild (or empty to remove it).
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeIcon(Optional<Image> icon) throws HTTP429Exception, DiscordException, MissingPermissionsException;

	/**
	 * Changes the AFK voice channel of the guild.
	 *
	 * @param channel The new AFK voice channel of the guild (or empty to remove it).
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeAFKChannel(Optional<IVoiceChannel> channel) throws HTTP429Exception, DiscordException, MissingPermissionsException;

	/**
	 * Changes the AFK timeout for the guild.
	 *
	 * @param timeout The new AFK timeout for the guild.
	 * @throws HTTP429Exception
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeAFKTimeout(int timeout) throws HTTP429Exception, DiscordException, MissingPermissionsException;

	/**
	 * This deletes this guild if and only if you are its owner, otherwise it throws a {@link MissingPermissionsException}.
	 *
	 * @throws DiscordException
	 * @throws HTTP429Exception
	 * @throws MissingPermissionsException
	 */
	void deleteGuild() throws DiscordException, HTTP429Exception, MissingPermissionsException;

	/**
	 * This leaves the guild, NOTE: it throws a {@link DiscordException} if you are the guilds owner, use
	 * {@link #deleteGuild()} instead!
	 *
	 * @throws DiscordException
	 * @throws HTTP429Exception
	 */
	void leaveGuild() throws DiscordException, HTTP429Exception;

	/**
	 * Creates a new channel.
	 *
	 * @param name The name of the new channel. MUST be between 2-100 characters long.
	 * @return The new channel.
	 *
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 * @throws HTTP429Exception
	 */
	IChannel createChannel(String name) throws DiscordException, MissingPermissionsException, HTTP429Exception;

	/**
	 * Creates a new voice channel.
	 *
	 * @param name The name of the new channel. MUST be between 2-100 characters long.
	 * @return The new channel.
	 *
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 * @throws HTTP429Exception
	 */
	IVoiceChannel createVoiceChannel(String name) throws DiscordException, MissingPermissionsException, HTTP429Exception;

	/**
	 * Gets the region this guild is located in.
	 *
	 * @return The region.
	 */
	IRegion getRegion();

	/**
	 * Transfers the ownership of this guild to another user.
	 *
	 * @param newOwner The new owner.
	 *
	 * @throws HTTP429Exception
	 * @throws MissingPermissionsException
	 * @throws DiscordException
	 */
	void transferOwnership(IUser newOwner) throws HTTP429Exception, MissingPermissionsException, DiscordException;

	/**
	 * This retrieves the @everyone role which exists on all guilds.
	 *
	 * @return The object representing the @everyone role.
	 */
	IRole getEveryoneRole();

	/**
	 * This gets all the currently available invites for this guild.
	 *
	 * @return The list of all available invites.
	 *
	 * @throws DiscordException
	 * @throws HTTP429Exception
	 */
	List<IInvite> getInvites() throws DiscordException, HTTP429Exception;

	/**
	 * This reorders the position of the roles in this guild.
	 *
	 * @param rolesInOrder ALL the roles in the server, in the order of desired position. The first role gets position 1, second position 2, etc.
	 *
	 * @throws DiscordException
	 * @throws HTTP429Exception
	 * @throws MissingPermissionsException
	 */
	void reorderRoles(IRole... rolesInOrder) throws DiscordException, HTTP429Exception, MissingPermissionsException;

	/**
	 * Gets the amount of users that would be pruned for the given amount of days.
	 *
	 * @param days The amount of days of inactivity to lead to a prune.
	 * @return The amount of users.
	 *
	 * @throws DiscordException
	 * @throws HTTP429Exception
	 */
	int getUsersToBePruned(int days) throws DiscordException, HTTP429Exception;

	/**
	 * Prunes guild users for the given amount of days.
	 *
	 * @param days The amount of days of inactivity to lead to a prune.
	 * @return The amount of users.
	 *
	 * @throws DiscordException
	 * @throws HTTP429Exception
	 */
	int pruneUsers(int days) throws DiscordException, HTTP429Exception;

	/**
	 * Attempts to add a bot to this guild.
	 *
	 * @param applicationID The OAuth2 application id for the application owning the bot.
	 * @param permissions The (optional) permissions for this bot to have when entering the guild.
	 *
	 * @throws MissingPermissionsException
	 * @throws DiscordException
	 * @throws HTTP429Exception
	 */
	void addBot(String applicationID, Optional<EnumSet<Permissions>> permissions) throws MissingPermissionsException, DiscordException, HTTP429Exception;

	/**
	 * Gets the audio channel of this guild. This throws an exception if the bot isn't in a channel yet.
	 *
	 * @return The audio channel.
	 *
	 * @throws DiscordException
	 */
	AudioChannel getAudioChannel() throws DiscordException;
}
