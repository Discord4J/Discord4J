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

package sx.blah.discord.handle.obj;

import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.audit.ActionType;
import sx.blah.discord.handle.audit.AuditLog;
import sx.blah.discord.util.Ban;
import sx.blah.discord.util.Image;

import java.time.Instant;
import java.util.List;

/**
 * A Discord guild.
 */
public interface IGuild extends IDiscordObject<IGuild> {

	/**
	 * Gets the unique snowflake ID of the owner of the guild.
	 *
	 * @return The unique snowflake ID of the owner of the guild.
	 */
	long getOwnerLongID();

	/**
	 * Gets the owner of the guild.
	 *
	 * @return The owner of the guild.
	 */
	IUser getOwner();

	/**
	 * Gets the guild's icon hash.
	 *
	 * @return The guild's icon hash.
	 */
	String getIcon();

	/**
	 * Gets the guild's icon URL.
	 *
	 * @return The guild's icon URL.
	 */
	String getIconURL();

	/**
	 * Gets the guild's text channels sorted by their effective positions.
	 *
	 * @return The guild's text channels sorted by their effective positions.
	 */
	List<IChannel> getChannels();

	/**
	 * Gets a text channel by its unique snowflake ID from the guild's text channel cache.
	 *
	 * @param id The ID of the desired channel.
	 * @return The text channel with the provided ID (or null if one was not found).
	 */
	IChannel getChannelByID(long id);

	/**
	 * Gets the guild's members.
	 *
	 * @return The guild's members.
	 */
	List<IUser> getUsers();

	/**
	 * Gets a user by its unique snowflake ID from the guild's user cache.
	 *
	 * @param id The ID of the desired user.
	 * @return The user with the provided ID (or null if one was not found).
	 */
	IUser getUserByID(long id);

	/**
	 * Gets a list of text channels by their name.
	 *
	 * @param name The case-sensitive name of the desired text channels.
	 * @return A list of text channels with the provided name.
	 */
	List<IChannel> getChannelsByName(String name);

	/**
	 * Gets a list of voice channels by their name.
	 *
	 * @param name The case-sensitive name of the desired voice channels.
	 * @return A list of voice channels with the provided name.
	 */
	List<IVoiceChannel> getVoiceChannelsByName(String name);

	/**
	 * Gets a list of users by their name.
	 *
	 * <p>This is equivalent to <code>getUsersByName(name, true)</code>
	 *
	 * @param name The case-sensitive name of the desired users.
	 * @return A list of users with the provided name.
	 */
	List<IUser> getUsersByName(String name);

	/**
	 * Gets a list of users by their name.
	 *
	 * @param name The name of the desired users.
	 * @param includeNicknames Whether to match nicknames as well as usernames.
	 * @return A list of users with the provided name.
	 */
	List<IUser> getUsersByName(String name, boolean includeNicknames);

	/**
	 * Gets a list of users with the given role.
	 *
	 * @param role The role of the desired users.
	 * @return A list of users with the given role.
	 */
	List<IUser> getUsersByRole(IRole role);

	/**
	 * Gets the name of the guild.
	 *
	 * @return The name of the guild.
	 */
	String getName();

	/**
	 * Gets the guild's roles sorted by their effective positions.
	 *
	 * @return The guild's roles sorted by their effective positions.
	 */
	List<IRole> getRoles();

	/**
	 * Gets the roles for the given user.
	 *
	 * @param user The user to get the roles for.
	 * @return The roles for the given user.
	 */
	List<IRole> getRolesForUser(IUser user);

	/**
	 * Gets a role by its unique snowflake ID from the guild's role cache.
	 *
	 * @param id The ID of the desired role.
	 * @return The role with the provided ID (or null if one was not found).
	 */
	IRole getRoleByID(long id);

	/**
	 * Gets a list of roles by their name.
	 *
	 * @param name The case-sensitive name of the desired roles.
	 * @return A list of roles with the provided name.
	 */
	List<IRole> getRolesByName(String name);

	/**
	 * Gets the guild's voice channels sorted by their effective positions.
	 *
	 * @return The guild's vocie channels sorted by their effective positions.
	 */
	List<IVoiceChannel> getVoiceChannels();

	/**
	 * Gets a voice channel by its unique snowflake ID from the guild's voice channel cache.
	 *
	 * @param id The ID of the desired channel.
	 * @return The voice channel with the provided ID (or null if one was not found).
	 */
	IVoiceChannel getVoiceChannelByID(long id);

	/**
	 * Gets the voice channel in the guild that the bot is connected to.
	 *
	 * @return The voice channel in the guild that the bot is connected to (or null if the bot is not connected to a
	 * voice channel in the guild).
	 */
	IVoiceChannel getConnectedVoiceChannel();

	/**
	 * Gets the voice channel where AFK users are moved to.
	 *
	 * @return The voice channel where AFK users are moved to (or null if one is not set).
	 */
	IVoiceChannel getAFKChannel();

	/**
	 * Gets the timeout (in seconds) before a user is moved to the AFK voice channel.
	 *
	 * @return The timeout (in seconds) before a user is moved to the AFK voice channel.
	 */
	int getAFKTimeout();

	/**
	 * Creates a role in the guild.
	 *
	 * @return The created role.
	 */
	IRole createRole();

	/**
	 * Gets the list of users who are banned from the guild.
	 *
	 * @return The list of users who are banned from the guild.
	 */
	List<IUser> getBannedUsers();

	/**
	 * Gets the list of bans for the guild.
	 *
	 * @return The list of bans for the guild.
	 */
	List<Ban> getBans();

	/**
	 * Bans a user from the guild.
	 *
	 * @param user The user to ban.
	 */
	void banUser(IUser user);

	/**
	 * Bans a user from the guild.
	 *
	 * @param user The user to ban.
	 * @param deleteMessagesForDays The number of days to delete messages from this user for.
	 */
	void banUser(IUser user, int deleteMessagesForDays);

	/**
	 * Bans a user from the guild.
	 *
	 * @param user The user to ban.
	 * @param reason The reason for banning. This may be at most {@value Ban#MAX_REASON_LENGTH} characters long.
	 */
	void banUser(IUser user, String reason);

	/**
	 * Bans a user from the guild.
	 *
	 * @param user The user to ban.
	 * @param reason The reason for banning. This may be at most {@value Ban#MAX_REASON_LENGTH} characters long.
	 * @param deleteMessagesForDays The number of days to delete messages from this user for.
	 */
	void banUser(IUser user, String reason, int deleteMessagesForDays);

	/**
	 * Bans a user from the guild.
	 *
	 * @param userID The snowflake ID of the user to ban.
	 */
	void banUser(long userID);

	/**
	 * Bans a user from the guild.
	 *
	 * @param userID The snowflake ID of the user to ban.
	 * @param deleteMessagesForDays The number of days to delete messages from this user for.
	 */
	void banUser(long userID, int deleteMessagesForDays);

	/**
	 * Bans a user from the guild.
	 *
	 * @param userID The snowflake ID of the user to ban.
	 * @param reason The reason for banning. This may be at most {@value Ban#MAX_REASON_LENGTH} characters long.
	 */
	void banUser(long userID, String reason);

	/**
	 * Bans a user from the guild.
	 *
	 * @param userID The snowflake ID of the user to ban.
	 * @param reason The reason for banning. This may be at most {@value Ban#MAX_REASON_LENGTH} characters long.
	 * @param deleteMessagesForDays The number of days to delete messages from this user for.
	 */
	void banUser(long userID, String reason, int deleteMessagesForDays);

	/**
	 * Unbans a user.
	 *
	 * @param userID The user to unban.
	 */
	void pardonUser(long userID);

	/**
	 * Kicks a user from the guild.
	 *
	 * @param user The user to kick.
	 */
	void kickUser(IUser user);

	/**
	 * Kicks a user from the guild.
	 *
	 * @param user The user to kick.
	 * @param reason The reason for kicking. This may be at most {@value Ban#MAX_REASON_LENGTH} characters long.
	 */
	void kickUser(IUser user, String reason);

	/**
	 * Edits the roles a user has.
	 *
	 * @param user The user to edit the roles for.
	 * @param roles The roles for the user to have.
	 */
	void editUserRoles(IUser user, IRole[] roles);

	/**
	 * Sets whether a user is deafened.
	 *
	 * @param user The user to deafen or undeafen.
	 * @param deafen Whether the user is deafened.
	 */
	void setDeafenUser(IUser user, boolean deafen);

	/**
	 * Sets whether a user is muted.
	 *
	 * @param user The user to mute or unmute.
	 * @param mute Whether the user is muted.
	 */
	void setMuteUser(IUser user, boolean mute);

	/**
	 * Sets a user's nickname in the guild.
	 *
	 * @param user The user to set the nickname for.
	 * @param nick The user's new nickname or null to remove the nickname.
	 */
	void setUserNickname(IUser user, String nick);

	/**
	 * Edits all properties of the guild.
	 *
	 * @param name The name of the guild.
	 * @param region The region of the guild.
	 * @param level The verification level of the guild.
	 * @param icon The icon of the guild.
	 * @param afkChannel The afk channel of the guild.
	 * @param afkTimeout The afk timeout of the guild.
	 */
	void edit(String name, IRegion region, VerificationLevel level, Image icon, IVoiceChannel afkChannel, int afkTimeout);

	/**
	 * Changes the name of the guild.
	 *
	 * @param name The name of the guild.
	 */
	void changeName(String name);

	/**
	 * Changes the region of the guild.
	 *
	 * @param region The region of the guild.
	 */
	void changeRegion(IRegion region);

	/**
	 * Changes the verification level of the guild.
	 *
	 * @param verification The verification level of the guild.
	 */
	void changeVerificationLevel(VerificationLevel verification);

	/**
	 * Changes the icon of the guild.
	 *
	 * @param icon The icon of the guild (or null to remove it).
	 */
	void changeIcon(Image icon);

	/**
	 * Changes the AFK voice channel of the guild.
	 *
	 * @param channel The AFK voice channel of the guild (or null to remove it).
	 */
	void changeAFKChannel(IVoiceChannel channel);

	/**
	 * Changes the AFK timeout for the guild.
	 *
	 * @param timeout The AFK timeout for the guild.
	 */
	void changeAFKTimeout(int timeout);

	/**
	 * Leaves the guild.
	 */
	void leave();

	/**
	 * Creates a new channel.
	 *
	 * @param name The name of the new channel. MUST be between 2-100 characters long.
	 * @return The new channel.
	 */
	IChannel createChannel(String name);

	/**
	 * Creates a new voice channel.
	 *
	 * @param name The name of the new voice channel. MUST be between 2-100 characters long.
	 * @return The new voice channel.
	 */
	IVoiceChannel createVoiceChannel(String name);

	/**
	 * Gets the guild's voice region.
	 *
	 * @return The guild's voice region.
	 */
	IRegion getRegion();

	/**
	 * Gets the guild's verification level.
	 *
	 * @return The guild's verification level.
	 */
	VerificationLevel getVerificationLevel();

	/**
	 * Gets the @everyone role for the guild.
	 *
	 * @return The @everyone role for the guild.
	 */
	IRole getEveryoneRole();

	/**
	 * Gets the channel in the guild with the highest position that the bot user can read.
	 *
	 * @return The channel in the guild with the highest position that the bot user can read.
	 */
	IChannel getDefaultChannel();

	/**
	 * Gets all of the invites to the guild.
	 *
	 * @return All of the invites to the guild.
	 */
	List<IExtendedInvite> getExtendedInvites();

	/**
	 * Reorders the position of the roles in the guild.
	 *
	 * @param rolesInOrder All the roles in the guild, in order. The lowest role is at position <code>0</code>.
	 */
	void reorderRoles(IRole... rolesInOrder);

	/**
	 * Gets the number of users that would be pruned for the given number of days of inactivity.
	 *
	 * @param days The number of days of inactivity.
	 * @return The number of users that would be pruned for the given number of days of inactivity.
	 */
	int getUsersToBePruned(int days);

	/**
	 * Prunes guild users for the given number of days of inactivity.
	 *
	 * @param days The number of days of inactivity.
	 * @return The number of users pruned.
	 */
	int pruneUsers(int days);

	/**
	 * Gets whether the guild is deleted.
	 *
	 * @return Whether the guild is deleted.
	 */
	boolean isDeleted();

	/**
	 * Gets the guild's audio manager.
	 *
	 * @return The guild's audio manager.
	 */
	IAudioManager getAudioManager();

	/**
	 * This gets the timestamp of when the given user joined the guild.
	 *
	 * @param user The user to get the timestamp for.
	 * @return The timestamp of when the given user joined the guild.
	 */
	Instant getJoinTimeForUser(IUser user);

	/**
	 * Gets a message by its unique snowflake ID from the guild's message cache.
	 *
	 * @param id The ID of the desired message.
	 * @return The message with the provided ID (or null if one was not found).
	 */
	IMessage getMessageByID(long id);

	/**
	 * Gets the guild's emojis.
	 *
	 * @return The guild's emojis.
	 */
	List<IEmoji> getEmojis();

	/**
	 * Gets an emoji by its unique snowflake ID from the guild's emoji cache.
	 *
	 * @param id The ID of the desired emoji.
	 * @return The emoji with the provided ID (or null if one was not found).
	 */
	IEmoji getEmojiByID(long id);

	/**
	 * Gets a custom emoji by its name.
	 *
	 * @param name The name of the desired emoji.
	 * @return The emoji with the given name (or null if one was not found).
	 */
	IEmoji getEmojiByName(String name);

	/**
	 * Creates a new emoji.
	 *
	 * @param name The name, <b>without colons</b> of length 2-32 characters only consisting of alphanumeric characters and underscores.
	 * @param image The image of the emoji.
	 * @param roles The roles for which this emoji will be whitelisted, if empty all roles will be allowed. Your bot must be whitelisted by Discord to use this feature.
	 * @return The new emoji.
	 */
	IEmoji createEmoji(String name, Image image, IRole[] roles);

	/**
	 * Gets a webhook by its unique snowflake ID from the channels's webhook cache.
	 *
	 * @param id The ID of the desired webhook.
	 * @return The webhook with the provided ID (or null if one was not found).
	 */
	IWebhook getWebhookByID(long id);

	/**
	 * Gets a list of webhooks by their name.
	 *
	 * @param name The name of the desired webhooks.
	 * @return A list of webhooks with the provided name.
	 */
	List<IWebhook> getWebhooksByName(String name);

	/**
	 * Gets all of the guild's webhooks.
	 *
	 * @return All of the guild's webhooks.
	 */
	List<IWebhook> getWebhooks();

	/**
	 * Gets the total number of members the guild has.
	 *
	 * <p>Note: This is not necessarily the same as <code>getUsers().getSize()</code> because users are asynchronously
	 * retrieved from Discord.
	 *
	 * @return The total number of members the guild has.
	 */
	int getTotalMemberCount();

	/**
	 * Gets the full audit log for the guild.
	 *
	 * @return The full audit log for the guild.
	 */
	AuditLog getAuditLog();

	/**
	 * Gets the audit log with entries for the given action type.
	 *
	 * @param actionType The action type of the desired entries.
	 * @return The audit log with entries for the given action type.
	 */
	AuditLog getAuditLog(ActionType actionType);

	/**
	 * Gets the audit log with entries with the given responsible user.
	 *
	 * @param user The responsible user of the desired entries.
	 * @return The audit log with entries with the given responsible user.
	 */
	AuditLog getAuditLog(IUser user);

	/**
	 * Gets the audit log with entries with the given responsible user and the given action type.
	 *
	 * @param user The responsible user of the desired entries.
	 * @param actionType The action type of the desired entries.
	 * @return The audit log with entries with the given responsible user and the given action type.
	 */
	AuditLog getAuditLog(IUser user, ActionType actionType);

	/**
	 * Creates a new category.
	 *
	 * @param name The name of the new category. MUST be between 2-100 characters long.
	 * @return The new category.
	 */
	ICategory createCategory(String name);

	/**
	 * Gets the guild's categories sorted by their effective positions.
	 *
	 * @return The guild's categories sorted by their effective positions.
	 */
	List<ICategory> getCategories();

	/**
	 * Gets a category by its unique snowflake ID from the guild's category cache.
	 *
	 * @param id The ID of the desired category.
	 * @return The category with the provided ID (or null if one was not found).
	 */
	ICategory getCategoryByID(long id);

	/**
	 * Gets a list of categories by their name.
	 *
	 * @param name The case-sensitive name of the desired categories.
	 * @return A list of categories with the provided name.
	 */
	List<ICategory> getCategoriesByName(String name);

	/**
	 * Gets the channel to which system messages are sent.
	 *
	 * @return The channel to which system messages are sent, may be null.
	 */
	IChannel getSystemChannel();
}
