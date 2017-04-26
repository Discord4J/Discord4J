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
import sx.blah.discord.handle.impl.obj.PrivateChannel;
import sx.blah.discord.util.*;
import sx.blah.discord.util.cache.LongMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines a text channel in a guild/server.
 */
public interface IChannel extends IDiscordObject<IChannel> {

	/**
	 * Gets the name of this channel.
	 *
	 * @return The channel name.
	 */
	String getName();

	/**
	 * Gets the messages in this channel.
	 *
	 * @return The list of messages in the channel.
	 * @deprecated See {@link MessageHistory}
	 */
	@Deprecated
	MessageList getMessages();

	/**
	 * Gets the last {@link #getMaxInternalCacheCount()} received messages since startup.
	 *
	 * @return The messages.
	 */
	MessageHistory getMessageHistory();

	/**
	 * Gets the specified number of received messages.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param messageCount The amount of messages to go back by.
	 * @return The messages.
	 */
	MessageHistory getMessageHistory(int messageCount);

	/**
	 * Gets the messages from a specified date to the beginning of this channel.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param startDate The date to start gathering messages from (inclusive).
	 * @return The messages.
	 */
	MessageHistory getMessageHistoryFrom(LocalDateTime startDate);

	/**
	 * Gets the messages from a specified date to the beginning of this channel.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param startDate The date to start gathering messages from (inclusive).
	 * @param maxMessageCount The max number of messages to retrieve.
	 * @return The messages.
	 */
	MessageHistory getMessageHistoryFrom(LocalDateTime startDate, int maxMessageCount);

	/**
	 * Gets the messages from now up until the specified date.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param endDate The date to stop gathering messages at (inclusive).
	 * @return The messages.
	 */
	MessageHistory getMessageHistoryTo(LocalDateTime endDate);

	/**
	 * Gets the messages from now up until the specified date.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param endDate The date to stop gathering messages at (inclusive).
	 * @param maxMessageCount The max number of messages to retrieve.
	 * @return The messages.
	 */
	MessageHistory getMessageHistoryTo(LocalDateTime endDate, int maxMessageCount);

	/**
	 * Gets the messages in the specified range of dates.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param startDate The date to start gathering messages from (inclusive).
	 * @param endDate The date to stop gathering messages at (inclusive).
	 * @return The messages.
	 */
	MessageHistory getMessageHistoryIn(LocalDateTime startDate, LocalDateTime endDate);

	/**
	 * Gets the messages in the specified range of dates.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param startDate The date to start gathering messages from (inclusive).
	 * @param endDate The date to stop gathering messages at (inclusive).
	 * @param maxMessageCount The max number of messages to retrieve.
	 * @return The messages.
	 */
	MessageHistory getMessageHistoryIn(LocalDateTime startDate, LocalDateTime endDate, int maxMessageCount);

	/**
	 * Gets the messages from a specified message id to the beginning of this channel.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param id The id to start gathering messages from (inclusive).
	 * @return The messages.
	 * @deprecated Use {@link #getMessageHistoryFrom(long)} instead
	 */
	@Deprecated
	default MessageHistory getMessageHistoryFrom(String id) {
		return getMessageHistoryFrom(Long.parseUnsignedLong(id));
	}

	/**
	 * Gets the messages from a specified message id to the beginning of this channel.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param id The id to start gathering messages from (inclusive).
	 * @return The messages.
	 */
	MessageHistory getMessageHistoryFrom(long id);

	/**
	 * Gets the messages from a specified message id to the beginning of this channel.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param id The id to start gathering messages from (inclusive).
	 * @param maxMessageCount The max number of messages to retrieve.
	 * @return The messages.
	 * @deprecated Use {@link #getMessageHistoryFrom(long, int)} instead
	 */
	@Deprecated
	default MessageHistory getMessageHistoryFrom(String id, int maxMessageCount) {
		return getMessageHistoryFrom(Long.parseUnsignedLong(id), maxMessageCount);
	}

	/**
	 * Gets the messages from a specified message id to the beginning of this channel.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param id The id to start gathering messages from (inclusive).
	 * @param maxMessageCount The max number of messages to retrieve.
	 * @return The messages.
	 */
	MessageHistory getMessageHistoryFrom(long id, int maxMessageCount);

	/**
	 * Gets the messages from now up until the specified message id.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param id The id to stop gathering messages at (inclusive).
	 * @return The messages.
	 * @deprecated Use {@link #getMessageHistoryTo(long)} instead
	 */
	@Deprecated
	default MessageHistory getMessageHistoryTo(String id) {
		return getMessageHistoryTo(Long.parseUnsignedLong(id));
	}

	/**
	 * Gets the messages from now up until the specified message id.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param id The id to stop gathering messages at (inclusive).
	 * @return The messages.
	 */
	MessageHistory getMessageHistoryTo(long id);

	/**
	 * Gets the messages from now up until the specified message id.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param id The id to stop gathering messages at (inclusive).
	 * @param maxMessageCount The max number of messages to retrieve.
	 * @return The messages.
	 * @deprecated Use {@link #getMessageHistoryTo(long, int)} instead
	 */
	@Deprecated
	default MessageHistory getMessageHistoryTo(String id, int maxMessageCount) {
		return getMessageHistoryTo(Long.parseUnsignedLong(id), maxMessageCount);
	}

	/**
	 * Gets the messages from now up until the specified message id.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param id The id to stop gathering messages at (inclusive).
	 * @param maxMessageCount The max number of messages to retrieve.
	 * @return The messages.
	 */
	MessageHistory getMessageHistoryTo(long id, int maxMessageCount);

	/**
	 * Gets the messages in the specified range of message ids.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param beginID The id to start gathering messages from (inclusive).
	 * @param endID The id to stop gathering messages at (inclusive).
	 * @return The messages.
	 * @deprecated Use {@link #getMessageHistoryIn(long, long)} instead
	 */
	@Deprecated
	default MessageHistory getMessageHistoryIn(String beginID, String endID) {
		return getMessageHistoryIn(Long.parseUnsignedLong(beginID), Long.parseUnsignedLong(endID));
	}

	/**
	 * Gets the messages in the specified range of message ids.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param beginID The id to start gathering messages from (inclusive).
	 * @param endID The id to stop gathering messages at (inclusive).
	 * @return The messages.
	 */
	MessageHistory getMessageHistoryIn(long beginID, long endID);

	/**
	 * Gets the messages in the specified range of message ids.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param beginID The id to start gathering messages from (inclusive).
	 * @param endID The id to stop gathering messages at (inclusive).
	 * @param maxMessageCount The max number of messages to retrieve.
	 * @return The messages.
	 * @deprecated Use {@link #getMessageHistoryIn(long, long, int)} instead
	 */
	@Deprecated
	default MessageHistory getMessageHistoryIn(String beginID, String endID, int maxMessageCount) {
		return getMessageHistoryIn(Long.parseUnsignedLong(beginID), Long.parseUnsignedLong(endID), maxMessageCount);
	}

	/**
	 * Gets the messages in the specified range of message ids.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param beginID The id to start gathering messages from (inclusive).
	 * @param endID The id to stop gathering messages at (inclusive).
	 * @param maxMessageCount The max number of messages to retrieve.
	 * @return The messages.
	 */
	MessageHistory getMessageHistoryIn(long beginID, long endID, int maxMessageCount);

	/**
	 * This attempts to get ALL messages in the channel.
	 * NOTE: This will usually take awhile to run, so it is recommended to only run this method if ABSOLUTELY
	 * necessary.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @return The messages.
	 */
	MessageHistory getFullMessageHistory();

	/**
	 * This will attempt to bulk delete as many messages as possible in this guild.
	 * NOTE: Bulk deletes can only be applied to messages less than 2 weeks old.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @return The messages deleted.
	 */
	List<IMessage> bulkDelete();

	/**
	 * This will attempt to bulk delete the specified messages in the list.
	 * NOTE: Bulk deletes can only be applied to messages less than 2 weeks old.
	 * NOTE: This can block the current thread if messages need to be requested from Discord instead of fetched from
	 * the internal cache.
	 *
	 * @param messages The messages to try and bulk delete.
	 * @return The messages actually deleted in the bulk delete.
	 */
	List<IMessage> bulkDelete(List<IMessage> messages);

	/**
	 * This gets the number of messages which can be cached by this channel.
	 *
	 * @return The number that can be cached.
	 */
	int getMaxInternalCacheCount();

	/**
	 * This gets the number of messages which are currently cached by this channel.
	 *
	 * @return The number that can is cached.
	 */
	int getInternalCacheCount();

	/**
	 * Gets a specific message by its id.
	 *
	 * @param messageID The message id.
	 * @return The message (if found).
	 * @deprecated Use {@link #getMessageByID(long)} instead
	 */
	@Deprecated
	default IMessage getMessageByID(String messageID) {
		if (messageID == null) return null;
		return getMessageByID(Long.parseUnsignedLong(messageID));
	}

	/**
	 * Gets a specific message by its id.
	 *
	 * @param messageID The message id.
	 * @return The message (if found).
	 */
	IMessage getMessageByID(long messageID);

	/**
	 * Gets the guild this channel is a part of.
	 *
	 * @return The guild.
	 */
	IGuild getGuild();

	/**
	 * Gets whether or not this channel is a private one–if it is a private one, this object is an instance of {@link PrivateChannel}.
	 *
	 * @return True if the channel is private, false if otherwise.
	 */
	boolean isPrivate();

	/**
	 * Gets whether or not the channel is marked as NSFW (Not Suitable For Work)
	 *
	 * @return True if the channel is NSFW, false if not.
	 */
	boolean isNSFW();

	/**
	 * Gets the topic for the channel.
	 *
	 * @return The channel topic (null if not set).
	 */
	String getTopic();

	/**
	 * Formats a string to be able to #mention this channel.
	 *
	 * @return The formatted string.
	 */
	String mention();

	/**
	 * Sends a message without tts to the desired channel.
	 *
	 * @param content The content of the message.
	 * @return The message object representing the sent message
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	IMessage sendMessage(String content);

	/**
	 * Sends a message to the desired channel.
	 *
	 * @param embed The embed object, built with {@link EmbedBuilder}
	 * @return The message object representing the sent message
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 *
	 * @see EmbedBuilder
	 */
	IMessage sendMessage(EmbedObject embed);

	/**
	 * Sends a message to the desired channel.
	 *
	 * @param content The content of the message.
	 * @param tts Whether the message should use tts or not.
	 * @return The message object representing the sent message
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	IMessage sendMessage(String content, boolean tts);

	/**
	 * Sends a message to the desired channel.
	 *
	 * @param content The content of the message.
	 * @param embed The embed object, built with {@link EmbedBuilder}
	 * @return The message object representing the sent message
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 *
	 * @see EmbedBuilder
	 */
	IMessage sendMessage(String content, EmbedObject embed);

	/**
	 * Sends a message to the desired channel.
	 *
	 * @param content The content of the message.
	 * @param embed The embed object, built with {@link EmbedBuilder}
	 * @param tts Whether the message should use tts or not.
	 * @return The message object representing the sent message
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 *
	 * @see EmbedBuilder
	 */
	IMessage sendMessage(String content, EmbedObject embed, boolean tts);

	/**
	 * Uploads a file to the channel.
	 *
	 * @param file The file to upload.
	 * @return The message created by this action.
	 *
	 * @throws FileNotFoundException
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	IMessage sendFile(File file) throws FileNotFoundException;

	/**
	 * Uploads multiple files to the channel.
	 *
	 * @param files The files to upload.
	 * @return The message created by this action.
	 * @throws FileNotFoundException
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	IMessage sendFiles(File... files) throws FileNotFoundException;

	/**
	 * Uploads a file to the channel with a message attached.
	 *
	 * @param content The content of the attached message.
	 * @param file The file to upload.
	 * @return The message created by this action.
	 *
	 * @throws FileNotFoundException
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	IMessage sendFile(String content, File file) throws FileNotFoundException;

	/**
	 * Uploads multiple files to the channel with a message attached.
	 *
	 * @param content The content of the attached message.
	 * @param files The files to upload.
	 * @return The message created by this action.
	 *
	 * @throws FileNotFoundException
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	IMessage sendFiles(String content, File... files) throws FileNotFoundException;

	/**
	 * Uploads a file to the channel with an attached message and option for tts.
	 * <br>
	 * If you are using an image file for the upload WITH use in the embed object, name the withImage URL <code>attachment://filename.png</code>
	 * where the filename has only alphanumerics. See {@link EmbedBuilder#withImage(String)}.
	 *
	 * @param file The input stream to upload.
	 * @param embed An optional embed object to send with the file.
	 * @return The message created by this action.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 *
	 * @see EmbedBuilder
	 * @see EmbedBuilder#withImage(String)
	 */
	IMessage sendFile(EmbedObject embed, File file) throws FileNotFoundException;

	/**
	 * Uploads files to the channel with an attached message and option for tts.
	 * <br>
	 * If you are using an image file for the upload WITH use in the embed object, name the withImage URL <code>attachment://filename.png</code>
	 * where the filename has only alphanumerics. See {@link EmbedBuilder#withImage(String)}.
	 *
	 * @param files The files to upload.
	 * @param embed An optional embed object to send with the file.
	 * @return The message created by this action.
	 *
	 * @throws FileNotFoundException
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 *
	 * @see EmbedBuilder
	 * @see EmbedBuilder#withImage(String)
	 */
	IMessage sendFiles(EmbedObject embed, File... files) throws FileNotFoundException;

	/**
	 * Uploads an InputStream to the channel with an attached message and option for tts.
	 *
	 * @param content The content of the attached message.
	 * @param file The input stream to upload.
	 * @param fileName The name of the file that should be shown in Discord.
	 * @return The message created by this action.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	IMessage sendFile(String content, InputStream file, String fileName);

	/**
	 * Uploads AttachmentPartEntries to the channel with an attached message and option for tts.
	 *
	 * @param content The content of the attached message.
	 * @param entries The attachments to upload.
	 * @return The message created by this action.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	IMessage sendFiles(String content, AttachmentPartEntry... entries);

	/**
	 * Uploads an InputStream to the channel with an attached message and option for tts.
	 * <br>
	 * If you are using an image file for the upload WITH use in the embed object, name the withImage URL <code>attachment://filename.png</code>
	 * where the filename has only alphanumerics. See {@link EmbedBuilder#withImage(String)}.
	 *
	 * @param file The input stream to upload.
	 * @param fileName The name of the file that should be shown in Discord.
	 * @param embed An optional embed object to send with the file. Build embed objects with {@link EmbedBuilder}.
	 * @return The message created by this action.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 *
	 * @see EmbedBuilder
	 * @see EmbedBuilder#withImage(String)
	 */
	IMessage sendFile(EmbedObject embed, InputStream file, String fileName);

	/**
	 * Uploads AttachmentPartEntries to the channel with an attached message and option for tts.
	 * <br>
	 * If you are using an image file for the upload WITH use in the embed object, name the withImage URL <code>attachment://filename.png</code>
	 * where the filename has only alphanumerics. See {@link EmbedBuilder#withImage(String)}.
	 *
	 * @param entries The entries to upload
	 * @param embed An optional embed object to send with the file. Build embed objects with {@link EmbedBuilder}.
	 * @return The message created by this action.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 *
	 * @see EmbedBuilder
	 * @see EmbedBuilder#withImage(String)
	 */
	IMessage sendFiles(EmbedObject embed, AttachmentPartEntry... entries);

	/**
	 * Uploads an InputStream to the channel with an attached message and option for tts.
	 *
	 * @param content The content of the attached message.
	 * @param tts Whether the message should use tts or not.
	 * @param file The input stream to upload.
	 * @param fileName The name of the file that should be shown in Discord.
	 * @return The message created by this action.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	IMessage sendFile(String content, boolean tts, InputStream file, String fileName);

	/**
	 * Send multiple attachments
	 * @param content The content of the attached message.
	 * @param tts Whether the message should use tts or not.
	 * @param entries The attachments to attach to this message.
	 * @return
	 */
	IMessage sendFiles(String content, boolean tts, AttachmentPartEntry... entries);

	/**
	 * Uploads an InputStream to the channel with an attached message and option for tts.
	 * <br>
	 * If you are using an image file for the upload WITH use in the embed object, name the withImage URL <code>attachment://filename.png</code>
	 * where the filename has only alphanumerics. See {@link EmbedBuilder#withImage(String)}.
	 *
	 * @param content The content of the attached message.
	 * @param tts Whether the message should use tts or not.
	 * @param file The input stream to upload.
	 * @param fileName The name of the file that should be shown in Discord.
	 * @param embed An optional embed object to send with the file. Build embed objects with {@link EmbedBuilder}.
	 * @return The message created by this action.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 *
	 * @see EmbedBuilder
	 * @see EmbedBuilder#withImage(String)
	 */
	IMessage sendFile(String content, boolean tts, InputStream file, String fileName, EmbedObject embed);

	/**
	 * Uploads AttachmentStreamEntries to the channel with an attached message and option for tts.
	 * <br>
	 * If you are using an image file for the upload WITH use in the embed object, name the withImage URL <code>attachment://filename.png</code>
	 * where the filename has only alphanumerics. See {@link EmbedBuilder#withImage(String)}.
	 *
	 * @param content The content of the attached message.
	 * @param tts Whether the message should use tts or not.
	 * @param entries The files to upload
	 * @param embed An optional embed object to send with the file. Build embed objects with {@link EmbedBuilder}.
	 * @return The message created by this action.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	IMessage sendFiles(String content, boolean tts, EmbedObject embed, AttachmentPartEntry... entries);

	/**
	 * Uploads an InputStream to the channel with an attached message and option for tts.
	 * <br>
	 * If you are using an image file for the upload WITH use in the embed object, name the withImage URL <code>attachment://filename.png</code>
	 * where the filename has only alphanumerics. See {@link EmbedBuilder#withImage(String)}.
	 *
	 * @param builder The MessageBuilder to use
	 * @param file The input stream to upload.
	 * @param fileName The name of the file that should be shown in Discord.
	 * @return The message created by this action.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	IMessage sendFile(MessageBuilder builder, InputStream file, String fileName);

	/**
	 * Generates an invite for this channel.
	 *
	 * @param maxAge How long the invite should be valid, setting it to 0 makes it last forever.
	 * @param maxUses The maximum uses for the invite, setting it to 0 makes the invite have unlimited uses.
	 * @param temporary Whether users admitted with this invite are temporary.
	 * @param unique If the invite should be unique (Discord will sometimes group "similar", infinite use invites together)
	 * @return The newly generated invite.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	IInvite createInvite(int maxAge, int maxUses, boolean temporary, boolean unique);

	/**
	 * Toggles whether the bot is "typing".
	 */
	void toggleTypingStatus();

	/**
	 * Sets whether the bot is "typing".
	 *
	 * @param typing True if the bot is typing.
	 */
	void setTypingStatus(boolean typing);

	/**
	 * Gets whether the bot is "typing".
	 *
	 * @return True if the bot is typing, false if otherwise.
	 */
	boolean getTypingStatus();

	/**
	 * Edits all properties of this channel.
	 *
	 * @param name The new name of the channel.
	 * @param position The new position of the channel.
	 * @param topic The new topic of the channel.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void edit(String name, int position, String topic);

	/**
	 * Changes the name of the channel
	 *
	 * @param name The new name of the channel.
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeName(String name);

	/**
	 * Changes the position of the channel
	 *
	 * @param position The new position of the channel.
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changePosition(int position);

	/**
	 * Changes the topic of the channel
	 *
	 * @param topic The new topic of the channel.
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeTopic(String topic);

	/**
	 * Gets the position of the channel on the channel list.
	 *
	 * @return The position.
	 */
	int getPosition();

	/**
	 * Deletes this channel.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void delete();

	/**
	 * Gets the permissions overrides for users. (Key = User id).
	 *
	 * @return The user permissions overrides for this channel.
	 * @deprecated Use {@link #getUserOverridesLong()} instead
	 */
	@Deprecated
	default Map<String, PermissionOverride> getUserOverrides() {
		Map<String, PermissionOverride> map = new HashMap<>();
		getUserOverridesLong().forEach((key, value)-> map.put(Long.toUnsignedString(key), value));
		return map;
	}

	/**
	 * Gets the permissions overrides for users. (Key = User id).
	 *
	 * @return The user permissions overrides for this channel.
	 */
	LongMap<PermissionOverride> getUserOverridesLong();

	/**
	 * Gets the permissions overrides for roles. (Key = Role id).
	 *
	 * @return The role permissions overrides for this channel.
	 * @deprecated Use {@link #getRoleOverridesLong()} instead
	 */
	@Deprecated
	default Map<String, PermissionOverride> getRoleOverrides() {
		Map<String, PermissionOverride> map = new HashMap<>();
		getRoleOverridesLong().forEach((key, value)-> map.put(Long.toUnsignedString(key), value));
		return map;
	}

	/**
	 * Gets the permissions overrides for roles. (Key = Role id).
	 *
	 * @return The role permissions overrides for this channel.
	 */
	LongMap<PermissionOverride> getRoleOverridesLong();

	/**
	 * Gets the permissions available for a user with all permission overrides taken into account.
	 *
	 * @param user The user to get the permissions for.
	 * @return The set of permissions.
	 */
	EnumSet<Permissions> getModifiedPermissions(IUser user);

	/**
	 * Gets the permissions available for a role with all permission overrides taken into account.
	 *
	 * @param role The role to get the permissions for.
	 * @return The set of permissions.
	 */
	EnumSet<Permissions> getModifiedPermissions(IRole role);

	/**
	 * Removes a permissions override on this channel.
	 *
	 * @param user The user whose override should be removed.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void removePermissionsOverride(IUser user);

	/**
	 * Removes a permissions override on this channel.
	 *
	 * @param role The role whose override should be removed.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void removePermissionsOverride(IRole role);

	/**
	 * Creates/edits permission overrides for this channel.
	 *
	 * @param role The role to create/edit the permission overrides for.
	 * @param toAdd The permissions to add.
	 * @param toRemove The permissions to remove.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void overrideRolePermissions(IRole role, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove);

	/**
	 * Creates/edits permission overrides for this channel.
	 *
	 * @param user The user to create/edit the permission overrides for.
	 * @param toAdd The permissions to add.
	 * @param toRemove The permissions to remove.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void overrideUserPermissions(IUser user, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove);

	/**
	 * This gets all the currently available invites for this channel.
	 *
	 * @return The list of all available invites.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 *
	 * @deprecated Use {@link #getExtendedInvites()} instead.
	 */
	@Deprecated
	List<IInvite> getInvites();

	/**
	 * Gets all the invites for this channel.
	 *
	 * @return The list invites for this channel.
	 */
	List<IExtendedInvite> getExtendedInvites();

	/**
	 * This gets the users with the ability to read this channel.
	 *
	 * @return The users in this channel.
	 */
	List<IUser> getUsersHere();

	/**
	 * Gets the pinned messages in this channel.
	 *
	 * @return The pinned messages.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	List<IMessage> getPinnedMessages();

	/**
	 * This pins the provided message to this channel.
	 *
	 * @param message The message to pin.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void pin(IMessage message);

	/**
	 * This unpins the provided message from this channel.
	 *
	 * @param message The message to unpin.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void unpin(IMessage message);

	/**
	 * Gets the webhooks for this channel.
	 *
	 * @return The webhooks.
	 */
	List<IWebhook> getWebhooks();

	/**
	 * This gets a webhook by its id.
	 *
	 * @param id The webhook id.
	 * @return The webhook or null if not found.
	 * @deprecated Use {@link #getWebhookByID(long)} instead
	 */
	@Deprecated
	default IWebhook getWebhookByID(String id) {
		if (id == null) return null;
		return getWebhookByID(Long.parseUnsignedLong(id));
	}

	/**
	 * This gets a webhook by its id.
	 *
	 * @param id The webhook id.
	 * @return The webhook or null if not found.
	 */
	IWebhook getWebhookByID(long id);

	/**
	 * This finds all the webhooks which have the same name as the provided one.
	 *
	 * @param name The name to search for.
	 * @return The webhooks with a matching name.
	 */
	List<IWebhook> getWebhooksByName(String name);

	/**
	 * This creates a webhook for this channel with the provided name and the default avatar
	 *
	 * @param name The default name for the webhook.
	 * @return The created webhook.
	 */
	IWebhook createWebhook(String name);

	/**
	 * This creates a webhook for this channel with the provided name and the provided avatar
	 *
	 * @param name   The default name for the webhook.
	 * @param avatar The default avatar for the webhook.
	 * @return The created webhook.
	 */
	IWebhook createWebhook(String name, Image avatar);

	/**
	 * This creates a webhook for this channel with the provided name and the provided avatar
	 *
	 * @param name   The default name for the webhook.
	 * @param avatar The default avatar for the webhook.
	 * @return The created webhook.
	 */
	IWebhook createWebhook(String name, String avatar);

	/**
	 * Checks to see if the this channel is deleted.
	 *
	 * @return True if this channel is deleted.
	 */
	boolean isDeleted();

	/**
	 * Represents specific permission overrides for a user/role in the channel.
	 */
	class PermissionOverride implements IIDLinkedObject {

		/**
		 * Permissions to add.
		 */
		protected final EnumSet<Permissions> allow;

		/**
		 * Permissions to remove.
		 */
		protected final EnumSet<Permissions> deny;

		/**
		 * The id of the object this is linked to.
		 */
		protected final long id;

		public PermissionOverride(EnumSet<Permissions> allow, EnumSet<Permissions> deny, long id) {
			this.allow = allow;
			this.deny = deny;
			this.id = id;
		}

		/**
		 * Gets the permissions to add to the user/role.
		 *
		 * @return The permissions.
		 */
		public EnumSet<Permissions> allow() {
			return allow;
		}

		/**
		 * Gets the permissions to remove from the user/role.
		 *
		 * @return The permissions.
		 */
		public EnumSet<Permissions> deny() {
			return deny;
		}

		@Override
		public boolean equals(Object other) {
			if (other == null)
				return false;

			if (!this.getClass().isAssignableFrom(other.getClass()))
				return false;

			if (!((PermissionOverride) other).deny.equals(this.deny)) return false;
			if (!((PermissionOverride) other).allow.equals(this.allow)) return false;

			return true;
		}

		@Override
		public String toString() {
			return "PermissionOverride (Allow: " + allow + ", Deny: " + deny + ")";
		}

		@Override
		public long getLongID() {
			return id;
		}
	}
}
