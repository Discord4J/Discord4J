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
 * This represents a READ-ONLY collection holding {@link IMessage}s. MessageHistory instances are NOT dynamically
 * updated by the client. Meaning it may contain deleted messages if you create a MessageHistory and store it before
 * someone deletes their message. Additionally, every MessageHistory instance is independent of other instances,
 * meaning that mutating this instance will not effect other instances even if they are from the same channel.
 *
 * @see IChannel#getMessageHistory()
 * @see IChannel#getFullMessageHistory()
 * @see IChannel#getMessageHistory(int)
 * @see IChannel#getMessageHistoryFrom(java.time.LocalDateTime)
 * @see IChannel#getMessageHistoryFrom(java.time.LocalDateTime, int)
 * @see IChannel#getMessageHistoryFrom(String)
 * @see IChannel#getMessageHistoryFrom(String, int)
 * @see IChannel#getMessageHistoryIn(java.time.LocalDateTime, java.time.LocalDateTime)
 * @see IChannel#getMessageHistoryIn(java.time.LocalDateTime, java.time.LocalDateTime, int)
 * @see IChannel#getMessageHistoryIn(String, String)
 * @see IChannel#getMessageHistoryIn(String, String, int)
 * @see IChannel#getMessageHistoryTo(java.time.LocalDateTime)
 * @see IChannel#getMessageHistoryTo(java.time.LocalDateTime, int)
 * @see IChannel#getMessageHistoryTo(String)
 * @see IChannel#getMessageHistoryTo(String, int)
 */
public class MessageHistory extends AbstractList<IMessage> implements List<IMessage>, RandomAccess {

	private final IMessage[] backing; //Backed by an array because they are faster than lists

	private MessageHistory(IMessage[] messages) {
		this.backing = messages;
	}

	public MessageHistory(Collection<IMessage> messages) {
		this(messages.stream().distinct().sorted(MessageComparator.REVERSED).toArray(IMessage[]::new));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IMessage get(int index) {
		return backing[index];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return backing.length;
	}

	/**
	 * This gets the earliest sent message in this set of messages.
	 *
	 * @return The earliest message.
	 */
	public IMessage getEarliestMessage() {
		IMessage[] sorted = Arrays.copyOf(backing, backing.length);
		Arrays.sort(sorted, MessageComparator.DEFAULT);
		return sorted[0];
	}

	/**
	 * This gets the latest sent message in this set of messages.
	 *
	 * @return The latest message.
	 */
	public IMessage getLatestMessage() {
		IMessage[] sorted = Arrays.copyOf(backing, backing.length);
		Arrays.sort(sorted, MessageComparator.REVERSED);
		return sorted[0];
	}

	/**
	 * This gets a message by its id.
	 *
	 * @param id The id.
	 * @return The message if found, else null.
	 * @deprecated Use {@link #get(long)} instead
	 */
	@Deprecated
	public IMessage get(String id) {
		if (id == null) return null;
		return get(Long.parseUnsignedLong(id));
	}

	/**
	 * This gets a message by its id.
	 *
	 * @param id The id.
	 * @return The message if found, else null.
	 */
	public IMessage get(long id) {
		return Arrays.stream(backing)
				.filter(msg -> msg.getLongID() == id)
				.findFirst()
				.orElse(null);
	}

	/**
	 * This checks if this has a message with the specified id stored.
	 *
	 * @param id The id to look for.
	 * @return True if the specified id is stored, false otherwise.
	 * @deprecated Use {@link #contains(long)} instead
	 */
	@Deprecated
	public boolean contains(String id) {
		if (id == null) return false;
		return contains(Long.parseUnsignedLong(id));
	}

	public boolean contains(long id) {
		return Arrays.stream(backing)
				.anyMatch(msg -> msg.getLongID() == id);
	}

	/**
	 * This creates a new instance of MessageHistory independent of this one.
	 *
	 * @return The new instance.
	 */
	public MessageHistory copy() {
		return new MessageHistory(Arrays.copyOf(backing, backing.length));
	}

	/**
	 * This creates a new instance of MessageHistory independent of this one as well as creating new and independent
	 * instances of the messages sorted.
	 *
	 * @return The new instance.
	 */
	public MessageHistory deepCopy() {
		IMessage[] copied = new IMessage[backing.length];
		for (int i = 0; i < backing.length; i++)
			copied[i] = backing[i].copy();
		return new MessageHistory(copied);
	}

	/**
	 * This gets the guild if the channel this belongs to has a guild.
	 *
	 * @return The guild, or null if the channel is not associated with a guild.
	 */
	public IGuild getGuild() {
		return getChannel().getGuild();
	}

	/**
	 * This gets the channel this history is associated with.
	 *
	 * @return The channel.
	 */
	public IChannel getChannel() {
		return backing[0].getChannel();
	}

	/**
	 * This gets the client this history is associated with.
	 *
	 * @return The client.
	 */
	public IDiscordClient getClient() {
		return backing[0].getClient();
	}

	/**
	 * This converts the history into an array.
	 *
	 * @return An independent array of {@link IMessage}s in the current order.
	 */
	public IMessage[] asArray() {
		return Arrays.copyOf(backing, backing.length);
	}

	/**
	 * This deletes the message at the specified index. This does NOT remove the deleted message from this
	 * MessageHistory instance.
	 *
	 * @param index The index to delete at.
	 * @return The message deleted.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	public IMessage delete(int index) {
		IMessage message = get(index);
		message.delete();
		return message;
	}

	/**
	 * This deletes the message with the specified id This does NOT remove the deleted message from thi MessageHistory
	 * instance.
	 *
	 * @param id The id of the message to delete.
	 * @return The message deleted or null if the message couldn't be found.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 * @deprecated Use {@link #delete(long)} instead
	 */
	@Deprecated
	public IMessage delete(String id) {
		if (id == null) return null;
		return delete(Long.parseUnsignedLong(id));
	}

	/**
	 * This deletes the message with the specified id This does NOT remove the deleted message from thi MessageHistory
	 * instance.
	 *
	 * @param id The id of the message to delete.
	 * @return The message deleted or null if the message couldn't be found.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	public IMessage delete(long id) {
		IMessage message = get(id);

		if (message == null)
			return null;

		message.delete();
		return message;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sort(Comparator<? super IMessage> c) {
		Arrays.sort(backing, c);
	}

	/**
	 * This attempts to bulk deletes the messages in this MessageHistory set.
	 *
	 * @return The messages that were deleted.
	 */
	public List<IMessage> bulkDelete() {
		return getChannel().bulkDelete(this);
	}
}
