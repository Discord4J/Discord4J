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

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.*;
import sx.blah.discord.util.cache.LongMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

/**
 * A text, voice, or private channel in Discord.
 */
public interface IChannel extends IDiscordObject<IChannel> {

	/**
	 * Gets the name of the channel.
	 *
	 * @return The name of the channel.
	 */
	String getName();

	/**
	 * Gets the cached messages in the channel. The max size of this list is determined by
	 * {@link sx.blah.discord.api.ClientBuilder#setMaxMessageCacheCount(int)}.
	 *
	 * @return The cached messages in the channel.
	 */
	MessageHistory getMessageHistory();

	/**
	 * Gets the specified number of messages.
	 *
	 * <p>If the internal message cache does not have enough messages, they will be fetched from Discord.
	 *
	 * @param messageCount The number of messages to retrieve.
	 * @return The specified number of messages.
	 */
	MessageHistory getMessageHistory(int messageCount);

	/**
	 * Gets the messages from a given date to the beginning of the channel.
	 *
	 * <p>If the internal message cache does not have enough messages, they will be fetched from Discord.
	 *
	 * @param startDate The date to start at. (Inclusive)
	 * @return The messages from a given date to the beginning of the channel.
	 */
	MessageHistory getMessageHistoryFrom(Instant startDate);

	/**
	 * Gets the messages from a given date to the beginning of the channel.
	 *
	 * <p>If the internal message cache does not have enough messages, they will be fetched from Discord.
	 *
	 * @param startDate The date to start at. (Inclusive)
	 * @param maxMessageCount The maximum number of messages to retrieve.
	 * @return The messages from a given date to the beginning of the channel.
	 */
	MessageHistory getMessageHistoryFrom(Instant startDate, int maxMessageCount);

	/**
	 * Gets the messages from a given message ID to the beginning of the channel.
	 *
	 * <p>If the internal message cache does not have enough messages, they will be fetched from Discord.
	 *
	 * @param id The ID to start gathering messages at. (Inclusive)
	 * @return The messages from a given message ID to the beginning of the channel.
	 */
	MessageHistory getMessageHistoryFrom(long id);

	/**
	 * Gets the messages from a given message ID to the beginning of the channel.
	 *
	 * <p>If the internal message cache does not have enough messages, they will be fetched from Discord.
	 *
	 * @param id The ID to start gathering messages at. (Inclusive)
	 * @param maxMessageCount The maximum number of messages to retrieve.
	 * @return The messages from a given message ID to the beginning of the channel.
	 */
	MessageHistory getMessageHistoryFrom(long id, int maxMessageCount);

	/**
	 * Gets the messages from the current time to the given date.
	 *
	 * <p>If the internal message cache does not have enough messages, they will be fetched from Discord.
	 *
	 * @param endDate The date to stop at. (Inclusive)
	 * @return The messages from the current time to the given date.
	 */
	MessageHistory getMessageHistoryTo(Instant endDate);

	/**
	 * Gets the messages from the current time to the given date.
	 *
	 * <p>If the internal message cache does not have enough messages, they will be fetched from Discord.
	 *
	 * @param endDate The date to stop at. (Inclusive)
	 * @param maxMessageCount The maximum number of messages to retrieve.
	 * @return The messages from the current time to the given date.
	 */
	MessageHistory getMessageHistoryTo(Instant endDate, int maxMessageCount);

	/**
	 * Gets the messages from the current time to the given message ID.
	 *
	 * <p>If the internal message cache does not have enough messages, they will be fetched from Discord.
	 *
	 * @param id The ID to stop gathering messages at. (Inclusive)
	 * @return The messages from the current time to the specified message ID.
	 */
	MessageHistory getMessageHistoryTo(long id);

	/**
	 * Gets the messages from the current time to the given message ID.
	 *
	 * <p>If the internal message cache does not have enough messages, they will be fetched from Discord.
	 *
	 * @param id The ID to stop gathering messages at. (Inclusive)
	 * @param maxMessageCount The maximum number of messages to retrieve.
	 * @return The messages from the current time to the specified message ID.
	 */
	MessageHistory getMessageHistoryTo(long id, int maxMessageCount);

	/**
	 * Gets the messages in the given range of dates.
	 *
	 * <p>If the internal message cache does not have enough messages, they will be fetched from Discord.
	 *
	 * @param startDate The date to start at. (Inclusive)
	 * @param endDate The date to stop at (Inclusive)
	 * @return The messages in the given range of dates.
	 */
	MessageHistory getMessageHistoryIn(Instant startDate, Instant endDate);

	/**
	 * Gets the messages in the specified range of dates.
	 *
	 * <p>If the internal message cache does not have enough messages, they will be fetched from Discord.
	 *
	 * @param startDate The date to start at. (Inclusive)
	 * @param endDate The date to stop at (Inclusive)
	 * @param maxMessageCount The maximum number of messages to retrieve.
	 * @return The messages in the given range of dates.
	 */
	MessageHistory getMessageHistoryIn(Instant startDate, Instant endDate, int maxMessageCount);

	/**
	 * Gets the messages in the given range of message IDs.
	 *
	 * <p>If the internal message cache does not have enough messages, they will be fetched from Discord.
	 *
	 * @param beginID The ID to start at (newest). (Inclusive)
	 * @param endID The ID to stop at (oldest). (Inclusive)
	 * @return The messages in the specified range of message IDs.
	 */
	MessageHistory getMessageHistoryIn(long beginID, long endID);

	/**
	 * Gets the messages in the given range of message IDs.
	 *
	 * <p>If the internal message cache does not have enough messages, they will be fetched from Discord.
	 *
	 * @param beginID The ID to start at (newest). (Inclusive)
	 * @param endID The ID to stop at (oldest). (Inclusive)
	 * @param maxMessageCount The maximum number of messages to retrieve.
	 * @return The messages in the given range of message IDs.
	 */
	MessageHistory getMessageHistoryIn(long beginID, long endID, int maxMessageCount);

	/**
	 * Gets all of the messages in the channel.
	 *
	 * <p>This is a potentially extremely expensive operation that can take a large amount of time to complete.
	 * <p>If the internal message cache does not have enough messages, they will be fetched from Discord.
	 *
	 * @return The messages.
	 */
	MessageHistory getFullMessageHistory();

	/**
	 * Bulk deletes as many messages as possible in the channel.
	 *
	 * <p>A message can be at most 2 weeks old to be eligible for bulk deletion.
	 * <p>If the internal message cache does not have enough messages, they will be fetched from Discord.
	 *
	 * @return The deleted messages.
	 */
	List<IMessage> bulkDelete();

	/**
	 * Bulk deletes the given messages.
	 *
	 * <p>A message can be at most 2 weeks old to be eligible for bulk deletion. If a message in the list is not
	 * eligible for bulk deletion, it will be ignored.
	 *
	 * @param messages The messages to delete.
	 * @return The deleted messages.
	 */
	List<IMessage> bulkDelete(List<IMessage> messages);

	/**
	 * Gets the maximum number of messages which can be cached in the channel.
	 *
	 * @return The maximum number of messages which can be cached in the channel.
	 */
	int getMaxInternalCacheCount();

	/**
	 * Gets the number of messages which are currently cached in the channel.
	 *
	 * @return The number of messages which are currently cached in the channel.
	 */
	int getInternalCacheCount();

	/**
	 * Gets a message by its unique snowflake ID from the channels's message cache.
	 *
	 * @param messageID The ID of the desired message.
	 * @return The message with the provided ID (or null if one was not found).
	 *
	 * @see #fetchMessage(long)
	 */
	IMessage getMessageByID(long messageID);

	/**
	 * Gets a message by its unique snowflake ID from the channels's message cache <b>or</b> by fetching it from Discord.
	 *
	 * <p>Discord allows fetching individual messages in a channel. This method first checks the channel's message cache
	 * and if there is no such message with the provided ID, it is requested from Discord.
	 *
	 * @param messageID The ID of the desired message.
	 * @return The message with the provided ID (or null if one was not found).
	 *
	 * @see #getMessageByID(long)
	 */
	IMessage fetchMessage(long messageID);

	/**
	 * Gets the parent guild of the channel.
	 *
	 * @return The parent guild of the channel.
	 */
	IGuild getGuild();

	/**
	 * Gets whether the channel is private. (A direct message channel with another user)
	 *
	 * @return Whether the channel is private.
	 */
	boolean isPrivate();

	/**
	 * Gets whether the channel is marked as NSFW (Not Safe For Work).
	 *
	 * @return Whether the channel is marked as NSFW.
	 */
	boolean isNSFW();

	/**
	 * Gets the topic for the channel.
	 *
	 * @return The channel topic (or null if one is not set).
	 */
	String getTopic();

	/**
	 * Gets a formatted string mentioning the channel.
	 *
	 * @return A formatted string mentioning the channel.
	 */
	String mention();

	/**
	 * Sends a message in the channel.
	 *
	 * @param content The content of the message.
	 * @return The sent message object.
	 */
	IMessage sendMessage(String content);

	/**
	 * Sends a message in the channel.
	 *
	 * @param embed The embed in the message.
	 * @return The sent message object.
	 *
	 * @see EmbedBuilder
	 */
	IMessage sendMessage(EmbedObject embed);

	/**
	 * Sends a message in the channel.
	 *
	 * @param content The content of the message.
	 * @param tts Whether the message should use text-to-speech.
	 * @return The sent message object.
	 */
	IMessage sendMessage(String content, boolean tts);

	/**
	 * Sends a message in the channel.
	 *
	 * @param content The content of the message.
	 * @param embed The embed in the message.
	 * @return The sent message object.
	 *
	 * @see EmbedBuilder
	 */
	IMessage sendMessage(String content, EmbedObject embed);

	/**
	 * Sends a message in the channel.
	 *
	 * @param content The content of the message.
	 * @param embed The embed in the message.
	 * @param tts Whether the message should use text-to-speech.
	 * @return The sent message object.
	 *
	 * @see EmbedBuilder
	 */
	IMessage sendMessage(String content, EmbedObject embed, boolean tts);

	/**
	 * Sends a message in the channel.
	 *
	 * @param file The attachment to send.
	 * @return The sent message object.
	 *
	 * @throws FileNotFoundException If the file is not found.
	 */
	IMessage sendFile(File file) throws FileNotFoundException;

	/**
	 * Sends a message in the channel.
	 *
	 * @param files The attachments to send.
	 * @return The sent message object.
	 *
	 * @throws FileNotFoundException If one of the files is not found.
	 */
	IMessage sendFiles(File... files) throws FileNotFoundException;

	/**
	 * Sends a message in the channel.
	 *
	 * @param content The content of the message.
	 * @param file The attachment to send.
	 * @return The sent message object.
	 *
	 * @throws FileNotFoundException If the file is not found.
	 */
	IMessage sendFile(String content, File file) throws FileNotFoundException;

	/**
	 * Sends a message in the channel.
	 *
	 * @param content The content of the message.
	 * @param files The attachments to send.
	 * @return The sent message object.
	 *
	 * @throws FileNotFoundException If one of the files is not found.
	 */
	IMessage sendFiles(String content, File... files) throws FileNotFoundException;

	/**
	 * Sends a message in the channel.
	 *
	 * <p>If the uploaded attachment is an image, it may be displayed in the embed using {@link EmbedBuilder#withImage(String)}.
	 * The image url should be formatted like so: <code>attachment://filename.extension</code> where filename consists
	 * of only alphanumeric characters.
	 *
	 * @param file The attachments to send.
	 * @param embed The embed in the message.
	 * @return The sent message object.
	 *
	 * @throws FileNotFoundException If the file is not found.
	 *
	 * @see EmbedBuilder
	 * @see EmbedBuilder#withImage(String)
	 */
	IMessage sendFile(EmbedObject embed, File file) throws FileNotFoundException;

	/**
	 * Sends a message in the channel.
	 *
	 * <p>If one of the uploaded attachments is an image, it may be displayed in the embed using {@link EmbedBuilder#withImage(String)}.
	 * The image url should be formatted like so: <code>attachment://filename.extension</code> where filename consists
	 * of only alphanumeric characters.
	 *
	 * @param files The attachments to send.
	 * @param embed The embed in the message.
	 * @return The sent message object.
	 *
	 * @throws FileNotFoundException If one of the files is not found.
	 *
	 * @see EmbedBuilder
	 * @see EmbedBuilder#withImage(String)
	 */
	IMessage sendFiles(EmbedObject embed, File... files) throws FileNotFoundException;

	/**
	 * Sends a message in the channel.
	 *
	 * @param content The content of the message.
	 * @param file The input stream to read and attach.
	 * @param fileName The name of the attachment that should be shown in Discord.
	 * @return The sent message object.
	 */
	IMessage sendFile(String content, InputStream file, String fileName);

	/**
	 * Sends a message in the channel.
	 *
	 * @param content The content of the message.
	 * @param entries The attachments to send.
	 * @return The sent message object.
	 */
	IMessage sendFiles(String content, AttachmentPartEntry... entries);

	/**
	 * Sends a message in the channel.
	 *
	 * <p>If the uploaded attachment is an image, it may be displayed in the embed using {@link EmbedBuilder#withImage(String)}.
	 * The image url should be formatted like so: <code>attachment://filename.extension</code> where filename consists
	 * of only alphanumeric characters.
	 *
	 * @param file The input stream to read and attach.
	 * @param fileName The name of the file that should be shown in Discord.
	 * @param embed The embed in the message.
	 * @return The sent message object.
	 *
	 * @see EmbedBuilder
	 * @see EmbedBuilder#withImage(String)
	 */
	IMessage sendFile(EmbedObject embed, InputStream file, String fileName);

	/**
	 * Sends a message in the channel.
	 *
	 * <p>If one of the uploaded attachments is an image, it may be displayed in the embed using {@link EmbedBuilder#withImage(String)}.
	 * The image url should be formatted like so: <code>attachment://filename.extension</code> where filename consists
	 * of only alphanumeric characters.
	 *
	 * @param entries The attachments to send.
	 * @param embed The embed in the message.
	 * @return The sent message object.
	 *
	 * @see EmbedBuilder
	 * @see EmbedBuilder#withImage(String)
	 */
	IMessage sendFiles(EmbedObject embed, AttachmentPartEntry... entries);

	/**
	 * Sends a message in the channel.
	 *
	 * @param content The content of the message.
	 * @param tts Whether the message should use text-to-speech.
	 * @param file The input stream to read and attach.
	 * @param fileName The name of the file that should be shown in Discord.
	 * @return The sent message object.
	 */
	IMessage sendFile(String content, boolean tts, InputStream file, String fileName);

	/**
	 * Sends a message in the channel.
	 *
	 * @param content The content of the message.
	 * @param tts Whether the message should use text-to-speech.
	 * @param entries The attachments to send.
	 * @return The sent message object.
	 */
	IMessage sendFiles(String content, boolean tts, AttachmentPartEntry... entries);

	/**
	 * Sends a message in the channel.
	 *
	 * <p>If the uploaded attachment is an image, it may be displayed in the embed using {@link EmbedBuilder#withImage(String)}.
	 * The image url should be formatted like so: <code>attachment://filename.extension</code> where filename consists
	 * of only alphanumeric characters.
	 *
	 * @param content The content of the message.
	 * @param tts Whether the message should use text-to-speech.
	 * @param file The input stream to read and attach.
	 * @param fileName The name of the file that should be shown in Discord.
	 * @param embed The embed in the message.
	 * @return The sent message object.
	 *
	 * @see EmbedBuilder
	 * @see EmbedBuilder#withImage(String)
	 */
	IMessage sendFile(String content, boolean tts, InputStream file, String fileName, EmbedObject embed);

	/**
	 * Sends a message in the channel.
	 *
	 * <p>If one of the uploaded attachments is an image, it may be displayed in the embed using {@link EmbedBuilder#withImage(String)}.
	 * The image url should be formatted like so: <code>attachment://filename.extension</code> where filename consists
	 * of only alphanumeric characters.
	 *
	 * @param content The content of the message.
	 * @param tts Whether the message should use text-to-speech.
	 * @param entries The attachments to send.
	 * @param embed The embed in the message.
	 * @return The sent message object.
	 *
	 * @see EmbedBuilder
	 * @see EmbedBuilder#withImage(String)
	 */
	IMessage sendFiles(String content, boolean tts, EmbedObject embed, AttachmentPartEntry... entries);

	/**
	 * Sends a message in the channel.
	 *
	 * <p>If the uploaded attachment is an image, it may be displayed in the embed using {@link EmbedBuilder#withImage(String)}.
	 * The image url should be formatted like so: <code>attachment://filename.extension</code> where filename consists
	 * of only alphanumeric characters.
	 *
	 * @param builder The MessageBuilder to get the rest of the message from.
	 * @param file The input stream to read and attach.
	 * @param fileName The name of the file that should be shown in Discord.
	 * @return The sent message object.
	 */
	IMessage sendFile(MessageBuilder builder, InputStream file, String fileName);

	/**
	 * Creates an invite to the channel.
	 *
	 * @param maxAge How long the invite should be valid, in seconds. <code>0</code> indicates infinite age.
	 * @param maxUses The maximum number of times the invite can be used. <code>0</code> indicates infinite uses.
	 * @param temporary Whether membership granted by the invite is temporary.
	 * @param unique If the invite must be unique. Discord will sometimes group "similar" invites together.
	 * @return The created invite.
	 */
	IInvite createInvite(int maxAge, int maxUses, boolean temporary, boolean unique);

	/**
	 * Toggles whether the bot is "typing".
	 */
	void toggleTypingStatus();

	/**
	 * Sets whether the bot is "typing".
	 *
	 * @param typing Whether the bot is typing.
	 */
	void setTypingStatus(boolean typing);

	/**
	 * Gets whether the bot is "typing".
	 *
	 * @return Whether the bot is typing.
	 */
	boolean getTypingStatus();

	/**
	 * Edits all properties of the channel.
	 *
	 * @param name The name of the channel.
	 * @param position The position of the channel.
	 * @param topic The topic of the channel.
	 */
	void edit(String name, int position, String topic);

	/**
	 * Changes the name of the channel.
	 *
	 * @param name The name of the channel.
	 */
	void changeName(String name);

	/**
	 * Changes the position of the channel.
	 *
	 * @param position The position of the channel.
	 */
	void changePosition(int position);

	/**
	 * Changes the topic of the channel.
	 *
	 * @param topic The topic of the channel.
	 */
	void changeTopic(String topic);

	/**
	 * Changes the nsfw state of the channel.
	 *
	 * @param isNSFW The new nsfw state of the channel.
	 */
	void changeNSFW(boolean isNSFW);

	/**
	 * Gets the position of the channel in the channel list.
	 *
	 * @return The position of the channel in the channel list.
	 */
	int getPosition();

	/**
	 * Deletes the channel.
	 */
	void delete();

	/**
	 * Gets the permissions overrides for users. (Key = User ID)
	 *
	 * @return The user permissions overrides for the channel.
	 */
	LongMap<sx.blah.discord.handle.obj.PermissionOverride> getUserOverrides();

	/**
	 * Gets the permissions overrides for roles. (Key = Role ID)
	 *
	 * @return The role permissions overrides for this channel.
	 */
	LongMap<sx.blah.discord.handle.obj.PermissionOverride> getRoleOverrides();

	/**
	 * Gets the permissions a user has in the channel, taking into account user and role overrides.
	 *
	 * @param user The user to get permissions for.
	 * @return The permissions the user has in the channel.
	 */
	EnumSet<Permissions> getModifiedPermissions(IUser user);

	/**
	 * Gets the permissions a role has in the channel, taking into account role overrides.
	 *
	 * @param role The role to get permissions for.
	 * @return The permissions the role has in the channel.
	 */
	EnumSet<Permissions> getModifiedPermissions(IRole role);

	/**
	 * Removes a user's permissions override.
	 *
	 * @param user The user whose override should be removed.
	 */
	void removePermissionsOverride(IUser user);

	/**
	 * Removes a role's permissions override.
	 *
	 * @param role The role whose override should be removed.
	 */
	void removePermissionsOverride(IRole role);

	/**
	 * Creates or edits a role's permissions override.
	 *
	 * @param role The role to create/edit the permissions override for.
	 * @param toAdd The permissions to explicitly grant.
	 * @param toRemove The permissions to explicitly deny.
	 */
	void overrideRolePermissions(IRole role, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove);

	/**
	 * Creates or edits a user's permissions override.
	 *
	 * @param user The user to create/edit the permissions override for.
	 * @param toAdd The permissions to explicitly grant.
	 * @param toRemove The permissions to explicitly deny.
	 */
	void overrideUserPermissions(IUser user, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove);

	/**
	 * Gets all the invites to the channel.
	 *
	 * @return A list of invites to the channel.
	 */
	List<IExtendedInvite> getExtendedInvites();

	/**
	 * Gets the users with read permissions in the channel.
	 *
	 * @return The users with read permissions in the channel.
	 */
	List<IUser> getUsersHere();

	/**
	 * Gets channel's pinned messages.
	 *
	 * @return The channel's pinned messages.
	 */
	List<IMessage> getPinnedMessages();

	/**
	 * Pins a message in the channel.
	 *
	 * @param message The message to pin.
	 */
	void pin(IMessage message);

	/**
	 * Unpins a message in the channel.
	 *
	 * @param message The message to unpin.
	 */
	void unpin(IMessage message);

	/**
	 * Gets the channel's webhooks.
	 *
	 * @return The channel's webhooks.
	 */
	List<IWebhook> getWebhooks();

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
	 * Creates a webhook for the channel.
	 *
	 * @param name The default name for the webhook.
	 * @return The created webhook.
	 */
	IWebhook createWebhook(String name);

	/**
	 * Creates a webhook for the channel.
	 *
	 * @param name The default name for the webhook.
	 * @param avatar The default avatar for the webhook.
	 * @return The created webhook.
	 */
	IWebhook createWebhook(String name, Image avatar);

	/**
	 * Creates a webhook for the channel.
	 *
	 * @param name The default name for the webhook.
	 * @param avatar The default avatar for the webhook.
	 * @return The created webhook.
	 */
	IWebhook createWebhook(String name, String avatar);

	/**
	 * Gets whether the channel is deleted.
	 *
	 * @return Whether the channel is deleted.
	 */
	boolean isDeleted();

	/**
	 * Changes the category of the channel.
	 *
	 * @param category The category of the channel.
	 */
	void changeCategory(ICategory category);

	/**
	 * Gets the category of the channel.
	 *
	 * @return The category of the channel, may be null.
	 */
	ICategory getCategory();
}
