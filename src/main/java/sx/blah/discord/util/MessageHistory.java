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

package sx.blah.discord.util;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

import java.util.*;

/**
 * A READ-ONLY collection of {@link IMessage messages}. A MessageHistory is a view of the requested range of messages at
 * the time is was created.
 *
 * @see IChannel#getMessageHistory()
 * @see IChannel#getFullMessageHistory()
 * @see IChannel#getMessageHistory(int)
 * @see IChannel#getMessageHistoryFrom(java.time.Instant)
 * @see IChannel#getMessageHistoryFrom(java.time.Instant, int)
 * @see IChannel#getMessageHistoryIn(java.time.Instant, java.time.Instant)
 * @see IChannel#getMessageHistoryIn(java.time.Instant, java.time.Instant, int)
 * @see IChannel#getMessageHistoryTo(java.time.Instant)
 * @see IChannel#getMessageHistoryTo(java.time.Instant, int)
 */
public class MessageHistory extends AbstractList<IMessage> implements List<IMessage>, RandomAccess {

	/**
	 * The underlying collection of messages.
	 */
	private final IMessage[] backing; //Backed by an array because they are faster than lists

	private MessageHistory(IMessage[] messages) {
		this.backing = messages;
	}

	public MessageHistory(Collection<IMessage> messages) {
		this(messages.stream().distinct().sorted(MessageComparator.REVERSED).toArray(IMessage[]::new));
	}

	@Override
	public IMessage get(int index) {
		return backing[index];
	}

	@Override
	public int size() {
		return backing.length;
	}

	/**
	 * Gets the oldest message in the collection.
	 *
	 * @return The oldest message in the collection.
	 */
	public IMessage getEarliestMessage() {
		return Arrays.stream(backing)
				.min(MessageComparator.DEFAULT)
				.orElse(null);
	}

	/**
	 * Gets the youngest message in the collection.
	 *
	 * @return The youngest message in the collection.
	 */
	public IMessage getLatestMessage() {
		return Arrays.stream(backing)
				.max(MessageComparator.DEFAULT)
				.orElse(null);
	}

	/**
	 * Gets a message by its unique snowflake ID.
	 *
	 * @param id The ID of the desired role.
	 * @return The message with the provided ID (or null if one was not found).
	 */
	public IMessage get(long id) {
		return Arrays.stream(backing)
				.filter(msg -> msg.getLongID() == id)
				.findFirst()
				.orElse(null);
	}

	/**
	 * Gets whether the collection contains a message with the given ID.
	 *
	 * @param id The ID to search for.
	 * @return Whether the collection contains a message with the given ID.
	 */
	public boolean contains(long id) {
		return Arrays.stream(backing)
				.anyMatch(msg -> msg.getLongID() == id);
	}

	/**
	 * Creates a copy of the collection.
	 *
	 * @return A copy of the collection.
	 */
	public MessageHistory copy() {
		return new MessageHistory(Arrays.copyOf(backing, backing.length));
	}

	/**
	 * Creates a copy of the collection made up of copies of the individual messages in the collection.
	 *
	 * @return A copy of the collection made up of copies of the individual messages in the collection.
	 */
	public MessageHistory deepCopy() {
		IMessage[] copied = new IMessage[backing.length];
		for (int i = 0; i < backing.length; i++)
			copied[i] = backing[i].copy();
		return new MessageHistory(copied);
	}

	/**
	 * The parent guild of the channel the messages were sent in, or null if no messages are present.
	 *
	 * @return The parent guild of the channel the messages were sent in, or null if no messages are present.
	 */
	public IGuild getGuild() {
		final IChannel channel = getChannel();
		return (channel == null) ? null : (channel.isPrivate() ? null : channel.getGuild());
	}

	/**
	 * Gets the channel the messages were sent in, or null if no messages are present.
	 *
	 * @return The channel the messages were sent in, or null if no messages are present.
	 */
	public IChannel getChannel() {
		return (backing.length == 0) ? null : backing[0].getChannel();
	}

	/**
	 * Gets the client the history belongs to, or null if no messages are present.
	 *
	 * @return The client the history belongs to, or null if no messages are present.
	 */
	public IDiscordClient getClient() {
		final IChannel channel = getChannel();
		return (channel == null) ? null : channel.getClient();
	}

	/**
	 * Gets the history as an array.
	 *
	 * @return The history as an array.
	 */
	public IMessage[] asArray() {
		return Arrays.copyOf(backing, backing.length);
	}

	/**
	 * Deletes the message at the specified index. The deleted message is NOT removed from the collection.
	 *
	 * @param index The index of the message to delete.
	 * @return The deleted message.
	 */
	public IMessage delete(int index) {
		IMessage message = get(index);
		message.delete();
		return message;
	}

	/**
	 * Deletes the message with the given ID. The deleted message is NOT removed from the collection.
	 *
	 * @param id The ID of the message to delete.
	 * @return The deleted message (or null if no message was found).
	 */
	public IMessage delete(long id) {
		IMessage message = get(id);

		if (message == null)
			return null;

		message.delete();
		return message;
	}

	@Override
	public void sort(Comparator<? super IMessage> c) {
		Arrays.sort(backing, c);
	}

	/**
	 * Bulk deletes the messages in the collection.
	 *
	 * @return The messages that were deleted.
	 * @see IChannel#bulkDelete()
	 */
	public List<IMessage> bulkDelete() {
		final IChannel channel = getChannel();
		return (channel == null) ? Collections.emptyList() : channel.bulkDelete(this);
	}
}
