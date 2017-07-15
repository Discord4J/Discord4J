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

import sx.blah.discord.util.*;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a voice channel.
 * <p>
 * Most methods from {@link IChannel}, when called, will always throw an exception due to the incompatible nature
 * between a text channel (what IChannel typically represents) and a voice channel. All deprecated methods defined by
 * this interface will throw an exception if invoked and should be avoided.
 */
public interface IVoiceChannel extends IChannel {
	/**
	 * This gets the maximum amount of users allowed in this voice channel.
	 *
	 * @return The maximum amount of users allowed (or 0 if there is not set limit)
	 */
	int getUserLimit();

	/**
	 * Gets the current bitrate of this voice channel.
	 *
	 * @return The bitrate of this voice channel in bits.
     */
	int getBitrate();

	/**
	 * Edits all properties of this voice channel.
	 *
	 * @param name The new name of the channel.
	 * @param position The new position of the channel.
	 * @param bitrate The new bitrate of the channel (in bits).
	 * @param userLimit The new user limit of the channel.
	 *
	 * @throws MissingPermissionsException
	 * @throws DiscordException
	 * @throws RateLimitException
	 */
	void edit(String name, int position, int bitrate, int userLimit);

	/**
	 * Changes the bitrate of the channel
	 *
	 * @param bitrate The new bitrate of the channel (in bits).
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeBitrate(int bitrate);

	/**
	 * Changes the user limit of the channel
	 *
	 * @param limit The new user limit of the channel.
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeUserLimit(int limit);

	/**
	 * Makes the bot user join this voice channel.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	void join();

	/**
	 * Makes the bot user leave this voice channel.
	 */
	void leave();

	/**
	 * Checks if this voice channel is connected to by our user.
	 *
	 * @return True if connected, false if otherwise.
	 */
	boolean isConnected();

	/**
	 * {@inheritDoc}
	 */
	IVoiceChannel copy();

	/**
	 * This collects all users connected to this voice channel and returns them in a list.
	 *
	 * @return The connected users.
	 */
	List<IUser> getConnectedUsers();

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageList getMessages();

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryFrom(LocalDateTime startDate, int maxCount);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryTo(LocalDateTime endDate, int maxCount);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryIn(LocalDateTime startDate, LocalDateTime endDate, int maxCount);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryFrom(long id, int maxCount);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryTo(long id, int maxCount);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryIn(long beginID, long endID, int maxCount);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistory();

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistory(int messageCount);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryFrom(LocalDateTime startDate);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryTo(LocalDateTime endDate);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryIn(LocalDateTime startDate, LocalDateTime endDate);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryFrom(long id);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryTo(long id);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryIn(long beginID, long endID);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getFullMessageHistory();

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	List<IMessage> bulkDelete();

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	List<IMessage> bulkDelete(List<IMessage> messages);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	int getMaxInternalCacheCount();

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	int getInternalCacheCount();

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	IMessage getMessageByID(long messageID);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	String getTopic();

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	IMessage sendMessage(String content);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	IMessage sendMessage(String content, boolean tts);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	IMessage sendFile(File file);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	IMessage sendFile(String content, File file);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	IMessage sendFile(String content, boolean tts, InputStream file, String fileName);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	void toggleTypingStatus();

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	boolean getTypingStatus();

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	void changeTopic(String topic);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	void edit(String name, int position, String topic);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	List<IMessage> getPinnedMessages();

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	List<IWebhook> getWebhooks();

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	IWebhook getWebhookByID(long id);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	List<IWebhook> getWebhooksByName(String name);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	IWebhook createWebhook(String name);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	IWebhook createWebhook(String name, Image avatar);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	IWebhook createWebhook(String name, String avatar);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	boolean isNSFW();
}
