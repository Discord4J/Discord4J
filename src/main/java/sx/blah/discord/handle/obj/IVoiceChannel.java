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

import sx.blah.discord.util.Image;
import sx.blah.discord.util.MessageHistory;

import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

/**
 * A voice channel in a {@link IGuild}.
 *
 * <p>Most methods from {@link IChannel}, when called, will always throw an exception due to the incompatible nature
 * between a text channel (what IChannel typically represents) and a voice channel. All deprecated methods defined by
 * this interface will throw an exception if invoked and should be avoided.
 */
public interface IVoiceChannel extends IChannel {
	/**
	 * Gets the maximum number of users allowed in the voice channel at once. <code>0</code> indicates no limit.
	 *
	 * @return The maximum number of users allowed in the voice channel at once.
	 */
	int getUserLimit();

	/**
	 * Gets the bitrate of the voice channel (in bits).
	 *
	 * @return The bitrate of the voice channel.
     */
	int getBitrate();

	/**
	 * Edits all properties of the voice channel.
	 *
	 * @param name The name of the channel.
	 * @param position The position of the channel.
	 * @param bitrate The bitrate of the channel (in bits).
	 * @param userLimit The user limit of the channel.
	 */
	void edit(String name, int position, int bitrate, int userLimit);

	/**
	 * Changes the bitrate of the channel.
	 *
	 * @param bitrate The bitrate of the channel (in bits).
	 */
	void changeBitrate(int bitrate);

	/**
	 * Changes the user limit of the channel.
	 *
	 * @param limit The user limit of the channel.
	 */
	void changeUserLimit(int limit);

	/**
	 * Makes the bot user join the voice channel.
	 */
	void join();

	/**
	 * Makes the bot user leave the voice channel.
	 */
	void leave();

	/**
	 * Gets whether the bot user is connected to the voice channel.
	 *
	 * @return Whether the bot user is connected to the voice channel.
	 */
	boolean isConnected();

	/**
	 * {@inheritDoc}
	 */
	IVoiceChannel copy();

	/**
	 * Gets the users who are connected to the voice channel.
	 *
	 * @return The users who are connected to the voice channel.
	 */
	List<IUser> getConnectedUsers();

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryFrom(Instant startDate, int maxCount);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryTo(Instant endDate, int maxCount);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryIn(Instant startDate, Instant endDate, int maxCount);

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
	MessageHistory getMessageHistoryFrom(Instant startDate);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryTo(Instant endDate);

	/**
	 * @deprecated See {@link IVoiceChannel} for details.
	 * @throws UnsupportedOperationException Impossible to use as a voice channel.
	 */
	@Override
	@Deprecated
	MessageHistory getMessageHistoryIn(Instant startDate, Instant endDate);

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
