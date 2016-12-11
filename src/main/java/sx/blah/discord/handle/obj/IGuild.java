package sx.blah.discord.handle.obj;

import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.audio.impl.AudioManager;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.time.LocalDateTime;
import java.util.List;

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
	 * Gets all the channels which has a name matching the provided one.
	 *
	 * @param name The name to search for.
	 * @return The list of matching channels.
	 */
	List<IChannel> getChannelsByName(String name);

	/**
	 * Gets all the voice channels which has a name matching the provided one.
	 *
	 * @param name The name to search for.
	 * @return The list of matching channels.
	 */
	List<IVoiceChannel> getVoiceChannelsByName(String name);

	/**
	 * Gets all the users which have a display name (i.e. nickname if present else discord name) which matches the
	 * provided name. This is effectively the same as #getUsersByName(name, true).
	 *
	 * @param name The name to search for.
	 * @return The list of matching users.
	 */
	List<IUser> getUsersByName(String name);

	/**
	 * Gets all the users which have a name which matches the.
	 * provided name.
	 *
	 * @param name The name to search for.
	 * @param includeNicknames Whether to check nicknames in addition to normal names.
	 * @return The list of matching users.
	 */
	List<IUser> getUsersByName(String name, boolean includeNicknames);

	/**
	 * Gets all the users who have the provided role.
	 *
	 * @param role The role to search with.
	 * @return The list of matching users.
	 */
	List<IUser> getUsersByRole(IRole role);

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
	 * Gets the roles a user is a part of.
	 *
	 * @param user The user to check the roles for.
	 * @return The roles.
	 */
	List<IRole> getRolesForUser(IUser user);

	/**
	 * Gets a role object for its unique id.
	 *
	 * @param id The role id of the desired role.
	 * @return The role, or null if not found.
	 */
	IRole getRoleByID(String id);

	/**
	 * This finds all the roles which has the same name as the provided one.
	 *
	 * @param name The name to search for.
	 * @return The roles with a matching name.
	 */
	List<IRole> getRolesByName(String name);

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
	 * Gets the voice channel that the bot is currently connected to.
	 *
	 * @return the voice channel (or null if the bot is not connected to a voice channel in this guild)
	 */
	IVoiceChannel getConnectedVoiceChannel();

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
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	IRole createRole() throws MissingPermissionsException, RateLimitException, DiscordException;

	/**
	 * Retrieves the list of banned users from this guild.
	 *
	 * @return The list of banned users.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	List<IUser> getBannedUsers() throws RateLimitException, DiscordException;

	/**
	 * Bans a user from this guild.
	 *
	 * @param user The user to ban.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void banUser(IUser user) throws MissingPermissionsException, RateLimitException, DiscordException;

	/**
	 * Bans a user from this guild.
	 *
	 * @param user The user to ban.
	 * @param deleteMessagesForDays The number of days to delete messages from this user for.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void banUser(IUser user, int deleteMessagesForDays) throws MissingPermissionsException, RateLimitException, DiscordException;

	/**
	 * Bans a user from this guild.
	 *
	 * @param userID The snowflake ID of the user.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void banUser(String userID) throws MissingPermissionsException, RateLimitException, DiscordException;

	/**
	 * Bans a user from this guild.
	 *
	 * @param userID The snowflake ID of the user.
	 * @param deleteMessagesForDays The number of days to delete messages from this user for.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void banUser(String userID, int deleteMessagesForDays) throws MissingPermissionsException, RateLimitException, DiscordException;

	/**
	 * This removes a ban on a user.
	 *
	 * @param userID The user to unban.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void pardonUser(String userID) throws MissingPermissionsException, RateLimitException, DiscordException;

	/**
	 * Kicks a user from the guild.
	 *
	 * @param user The user to kick.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void kickUser(IUser user) throws MissingPermissionsException, RateLimitException, DiscordException;

	/**
	 * Edits the roles a user is a part of.
	 *
	 * @param user The user to edit the roles for.
	 * @param roles The roles for the user to have.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void editUserRoles(IUser user, IRole[] roles) throws MissingPermissionsException, RateLimitException, DiscordException;

	/**
	 * Sets whether a user should be deafened.
	 *
	 * @param user The user affected.
	 * @param deafen True to deafen the user, false to undeafen the user.
	 *
	 * @throws MissingPermissionsException
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	void setDeafenUser(IUser user, boolean deafen) throws MissingPermissionsException, DiscordException, RateLimitException;

	/**
	 * Sets whether a user should be muted.
	 *
	 * @param user The user affected.
	 * @param mute True to mute the user, false to unmute the user.
	 *
	 * @throws MissingPermissionsException
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	void setMuteUser(IUser user, boolean mute) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Sets a user's nickname in this guild.
	 *
	 * @param user The user affected.
	 * @param nick The user's new nickname or null to remove the nickname.
	 *
	 * @throws MissingPermissionsException
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	void setUserNickname(IUser user, String nick) throws MissingPermissionsException, DiscordException, RateLimitException;

	/**
	 * Changes the name of the guild.
	 *
	 * @param name The new name of the guild.
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeName(String name) throws RateLimitException, DiscordException, MissingPermissionsException;

	/**
	 * Changes the region of the guild.
	 *
	 * @param region The new region of the guild.
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeRegion(IRegion region) throws RateLimitException, DiscordException, MissingPermissionsException;

	/**
	 * Changes the verification level of the guild.
	 *
	 * @param verification The new verification level of the guild.
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeVerificationLevel(VerificationLevel verification) throws RateLimitException, DiscordException, MissingPermissionsException;

	/**
	 * Changes the name of the guild.
	 *
	 * @param icon The new icon of the guild (or null to remove it).
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeIcon(Image icon) throws RateLimitException, DiscordException, MissingPermissionsException;

	/**
	 * Changes the AFK voice channel of the guild.
	 *
	 * @param channel The new AFK voice channel of the guild (or null to remove it).
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeAFKChannel(IVoiceChannel channel) throws RateLimitException, DiscordException, MissingPermissionsException;

	/**
	 * Changes the AFK timeout for the guild.
	 *
	 * @param timeout The new AFK timeout for the guild.
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeAFKTimeout(int timeout) throws RateLimitException, DiscordException, MissingPermissionsException;

	/**
	 * This deletes this guild if and only if you are its owner, otherwise it throws a {@link MissingPermissionsException}.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	void deleteGuild() throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * This leaves the guild, NOTE: it throws a {@link DiscordException} if you are the guilds owner, use
	 * {@link #deleteGuild()} instead!
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	void leaveGuild() throws DiscordException, RateLimitException;

	/**
	 * Creates a new channel.
	 *
	 * @param name The name of the new channel. MUST be between 2-100 characters long.
	 * @return The new channel.
	 *
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 */
	IChannel createChannel(String name) throws DiscordException, MissingPermissionsException, RateLimitException;

	/**
	 * Creates a new voice channel.
	 *
	 * @param name The name of the new channel. MUST be between 2-100 characters long.
	 * @return The new channel.
	 *
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 */
	IVoiceChannel createVoiceChannel(String name) throws DiscordException, MissingPermissionsException, RateLimitException;

	/**
	 * Gets the region this guild is located in.
	 *
	 * @return The region.
	 */
	IRegion getRegion();

	/**
	 * Gets the verification level of this guild.
	 *
	 * @return The verification level.
	 */
	VerificationLevel getVerificationLevel();

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
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	List<IInvite> getInvites() throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * This reorders the position of the roles in this guild.
	 *
	 * @param rolesInOrder ALL the roles in the server, in the order of desired position. The first role gets position 1, second position 2, etc.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	void reorderRoles(IRole... rolesInOrder) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Gets the amount of users that would be pruned for the given amount of days.
	 *
	 * @param days The amount of days of inactivity to lead to a prune.
	 * @return The amount of users.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	int getUsersToBePruned(int days) throws DiscordException, RateLimitException;

	/**
	 * Prunes guild users for the given amount of days.
	 *
	 * @param days The amount of days of inactivity to lead to a prune.
	 * @return The amount of users.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	int pruneUsers(int days) throws DiscordException, RateLimitException;

	/**
	 * Checks to see if the this guild is deleted.
	 *
	 * @return True if this guild is deleted.
	 */
	boolean isDeleted();

	/**
	 * Gets the {@link AudioManager} instance for this guild.
	 *
	 * @return The audio manager for this guild.
	 */
	IAudioManager getAudioManager();

	/**
	 * This gets the timestamp for when the provided user joined the guild.
	 *
	 * @param user The user to get the timestamp for.
	 * @return The timestamp.
	 *
	 * @throws DiscordException
	 */
	LocalDateTime getJoinTimeForUser(IUser user) throws DiscordException;

	/**
	 * This gets a message by its id.
	 *
	 * @param id The message id.
	 * @return The message or null if not found.
	 */
	IMessage getMessageByID(String id);

	/**
	 * This gets all the emojis in the guild.
	 *
	 * @return A list of emojis.
	 */
	List<IEmoji> getEmojis();

	/**
	 * This gets an emoji by its ID.
	 *
	 * @param id The ID.
	 * @return The emoji.
	 */
	IEmoji getEmojiByID(String id);

	/**
	 * This gets an emoji by its name.
	 *
	 * @param name The name, <b>without colons</b>.
	 * @return The emoji.
	 */
	IEmoji getEmojiByName(String name);

	/**
	 * This gets a webhook by its id.
	 *
	 * @param id The webhook id.
	 * @return The webhook or null if not found.
	 */
	IWebhook getWebhookByID(String id);

	/**
	 * This finds all the webhooks which have the same name as the provided one.
	 *
	 * @param name The name to search for.
	 * @return The webhooks with a matching name.
	 */
	List<IWebhook> getWebhooksByName(String name);

	/**
	 * This returns all the webhooks for this guild.
	 *
	 * @return All webhooks for this guild.
	 */
	List<IWebhook> getWebhooks();

	/**
	 * Get the total amount of members on the guild
	 *
	 * @return The count of members on the guild
	 */
	int getTotalMemberCount();
}
