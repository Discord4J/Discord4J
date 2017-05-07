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

import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sx.blah.discord.handle.impl.obj.Channel.MESSAGE_CHUNK_COUNT;

/**
 * This is a utility class for building message histories in a readable, easy-to-understand fashion. It can be built
 * into a {@link MessageHistory} object, a {@link Stream} of {@link IMessage} objects, or an {@link Iterator} of
 * {@link IMessage} objects. It can also be directly used in a for-each loop without being built.
 *
 * @see MessageHistory
 * @see MessageHistoryIterator
 */
public class MessageHistoryBuilder implements Iterable<IMessage> {

	private Channel channel;

	// User provided info
	private IMessage uStart; // Null if not given
	private LocalDateTime uStartTime; // null signifies most recent
	private boolean uIncludeStart;
	private IMessage uEnd; // Null if not given
	private LocalDateTime uEndTime; // null signifies channel start
	private boolean uIncludeEnd;
	private int count = -1; // < 0 signifies no limit

	// Used for builds
	private List<IMessage> cached;
	private List<IMessage> messages;
	private MessageHistoryRange range;
	private int currentCount;

	public MessageHistoryBuilder(IChannel channel) {
		this.channel = (Channel) channel;
	}

	/**
	 * Specifies the message to start at (inclusive).
	 *
	 * @param startID the message ID to start at
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder startAt(long startID) {
		this.uStart = channel.getMessageByID(startID);
		if (uStart == null)
			throw new IllegalArgumentException("Message " + Long.toUnsignedString(startID) + " does not exist in channel " + channel.getStringID() + "!");
		this.uStartTime = uStart.getTimestamp();
		this.uIncludeStart = true;
		return this;
	}

	/**
	 * Specifies the message to start at (inclusive).
	 *
	 * @param msg the message to start at
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder startAt(IMessage msg) {
		if (!channel.equals(msg.getChannel()))
			throw new IllegalArgumentException("Message " + msg.getStringID() + " does not exist in channel " + channel.getStringID() + "!");
		this.uStart = msg;
		this.uStartTime = uStart.getTimestamp();
		this.uIncludeStart = true;
		return this;
	}

	/**
	 * Specifies the time to start at (inclusive).
	 * NOTE: Using this method to set a start time chronologically before endAt, and using withMaxCount > 0, may
	 * result in SIGNIFICANT performance loss.
	 *
	 * @param startTime the time to start at
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder startAt(LocalDateTime startTime) {
		this.uStart = null;
		this.uStartTime = startTime;
		this.uIncludeStart = true;
		return this;
	}

	/**
	 * Specifies the message to start at.
	 *
	 * @param startID   the message ID to start at
	 * @param inclusive whether or not to include the specified message
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder startAt(long startID, boolean inclusive) {
		this.uStart = channel.getMessageByID(startID);
		if (uStart == null)
			throw new IllegalArgumentException("Message " + Long.toUnsignedString(startID) + " does not exist in channel " + channel.getStringID() + "!");
		this.uStartTime = uStart.getTimestamp();
		this.uIncludeStart = inclusive;
		return this;
	}

	/**
	 * Specifies the message to start at.
	 *
	 * @param msg       the message to start at
	 * @param inclusive whether or not to include the specified message
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder startAt(IMessage msg, boolean inclusive) {
		if (!channel.equals(msg.getChannel()))
			throw new IllegalArgumentException("Message " + msg.getStringID() + " does not exist in channel " + channel.getStringID() + "!");
		this.uStart = msg;
		this.uStartTime = uStart.getTimestamp();
		this.uIncludeStart = inclusive;
		return this;
	}

	/**
	 * Specifies the time to start at.
	 * NOTE: Using this method to set a start time chronologically before endAt, and using withMaxCount > 0, may
	 * result in SIGNIFICANT performance loss.
	 *
	 * @param startTime the time to start at
	 * @param inclusive whether or not to include the specified message
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder startAt(LocalDateTime startTime, boolean inclusive) {
		this.uStart = null;
		this.uStartTime = startTime;
		this.uIncludeStart = inclusive;
		return this;
	}

	/**
	 * Specifies the message to end at (exclusive).
	 *
	 * @param endID the message ID to end at
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder endAt(long endID) {
		this.uEnd = channel.getMessageByID(endID);
		if (uEnd == null)
			throw new IllegalArgumentException("Message " + Long.toUnsignedString(endID) + " does not exist in channel " + channel.getStringID() + "!");
		this.uEndTime = uEnd.getTimestamp();
		this.uIncludeEnd = false;
		return this;
	}

	/**
	 * Specifies the message to end at (exclusive).
	 *
	 * @param msg the message ID to end at
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder endAt(IMessage msg) {
		if (!channel.equals(msg.getChannel()))
			throw new IllegalArgumentException("Message " + msg.getStringID() + " does not exist in channel " + channel.getStringID() + "!");
		this.uEnd = msg;
		this.uEndTime = msg.getTimestamp();
		this.uIncludeEnd = false;
		return this;
	}

	/**
	 * Specifies the time to end at (exclusive).
	 *
	 * @param endTime the time to end at
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder endAt(LocalDateTime endTime) {
		this.uEnd = null;
		this.uEndTime = endTime;
		this.uIncludeEnd = false;
		return this;
	}

	/**
	 * Specifies the message to end at.
	 *
	 * @param endID     the message ID to end at
	 * @param inclusive whether or not to include this time
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder endAt(long endID, boolean inclusive) {
		this.uEnd = channel.getMessageByID(endID);
		if (uEnd == null)
			throw new IllegalArgumentException("Message " + Long.toUnsignedString(endID) + " does not exist in channel " + channel.getStringID() + "!");
		this.uEndTime = uEnd.getTimestamp();
		this.uIncludeEnd = inclusive;
		return this;
	}

	/**
	 * Specifies the message to end at.
	 *
	 * @param msg       the message ID to end at
	 * @param inclusive whether or not to include this time
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder endAt(IMessage msg, boolean inclusive) {
		if (!channel.equals(msg.getChannel()))
			throw new IllegalArgumentException("Message " + msg.getStringID() + " does not exist in channel " + channel.getStringID() + "!");
		this.uEnd = msg;
		this.uEndTime = msg.getTimestamp();
		this.uIncludeEnd = inclusive;
		return this;
	}

	/**
	 * Specifies the time to end at.
	 *
	 * @param endTime   the time to end at
	 * @param inclusive whether or not to include this time
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder endAt(LocalDateTime endTime, boolean inclusive) {
		this.uEnd = null;
		this.uEndTime = endTime;
		this.uIncludeEnd = inclusive;
		return this;
	}

	/**
	 * Specifies the maximum number of messages to get. Defaults to -1 (no limit).
	 *
	 * @param count max number of messages to get
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder withMaxCount(int count) {
		if (count < 1) throw new IllegalArgumentException();
		this.count = count;
		return this;
	}

	/**
	 * Returns a stream of the message history.
	 *
	 * @return the stream of the message history
	 * @see MessageHistoryIterator
	 */
	public Stream<IMessage> stream() {

		buildRange();

		return new MessageHistoryIterator(range).stream();
	}

	/**
	 * Returns an iterator for this message history.
	 * NOTE: This object can be put directly in a for-each loop.
	 *
	 * @return an Iterator instance for the message history
	 */
	@Override
	public Iterator<IMessage> iterator() {

		buildRange();

		return new MessageHistoryIterator(range);
	}

	/**
	 * Requests a {@link MessageHistory} object with the parameters set in this builder.
	 * NOTE: This will not block the thread during execution. Use {@link MessageHistoryBuilder#get()} to block the
	 * thread.
	 *
	 * @return the message history
	 * @see MessageHistory
	 */
	public RequestBuffer.RequestFuture<MessageHistory> request() {
		return RequestBuffer.request(() -> {
			return this.get();
		});
	}

	/**
	 * Builds a {@link MessageHistory} object with the parameters set in this builder.
	 * NOTE: This will block the thread until all messages are received. Use {@link MessageHistoryBuilder#request()}
	 * to request this message history without blocking.
	 *
	 * @return the message history
	 * @see MessageHistory
	 */
	public MessageHistory get() {

		// Get cache from the channel, sorted
		cached = channel.messages.stream().sorted(MessageComparator.REVERSED).collect(Collectors.toList());

		buildRange();

		// Initialize messages
		messages = count < 0 ?
				new ArrayList<>(MESSAGE_CHUNK_COUNT) :
				new ArrayList<>(count);

		// Checks for special case for efficiency- if a range is reverse ordered (start earlier in history than end) but
		// count unlimited, then it is equivalent to switching endpoints and reversing the collection afterwards
		if (range.isChronological() && count < 0) {

			range = new MessageHistoryRange(channel,
					uEnd == null ?
							new MessageHistoryRange.Endpoint(uEndTime, uIncludeEnd) :
							new MessageHistoryRange.Endpoint(uEnd, uIncludeEnd),
					uStart == null ?
							new MessageHistoryRange.Endpoint(uStartTime, uIncludeStart) :
							new MessageHistoryRange.Endpoint(uStart, uIncludeStart)
			);

			fetch();

			Collections.reverse(messages);
		} else {
			fetch();
		}

		return new MessageHistory(messages);
	}

	private void buildRange() {
		range = new MessageHistoryRange(channel,
				uStart == null ?
						uStartTime == null ?
								MessageHistoryRange.Endpoint.NOW :
								new MessageHistoryRange.Endpoint(uStartTime, uIncludeStart) :
						new MessageHistoryRange.Endpoint(uStart, uIncludeStart),
				uEnd == null ?
						uEndTime == null ?
								MessageHistoryRange.Endpoint.CHANNEL_CREATE :
								new MessageHistoryRange.Endpoint(uEndTime, uIncludeEnd) :
						new MessageHistoryRange.Endpoint(uEnd, uIncludeEnd)
		);
	}

	private void fetch() {

		// fetch first message from the range
		IMessage last = range.fetchFirstInRange();

		if (last == null) { // Means out of range start
			messages = new ArrayList<>();
			return;
		}

		if (count < 0 || currentCount++ < count) {
			messages.add(last); // Add first message
		} else return; // only happens if count == 0 so we should return

		// add any messages from cache
		int index = cached.indexOf(last);
		if (index >= 0) {
			IMessage[] toAdd = cached.subList(index + 1, cached.size()).toArray(new IMessage[0]);
			if (toAdd.length > 0) {
				if (!add(toAdd)) return;
				last = toAdd[toAdd.length - 1];
			}
		}

		IMessage[] chunk;

		do {
			// Get a chunk
			chunk = fetchHistory(last.getLongID());

			if (chunk == null || chunk.length == 0) return; // no more messages/reached end or beginning of channel

			// get new starting point- last message of list for normal, first message if reversed
			last = chunk[range.isChronological() ? 0 : chunk.length - 1];
		} while (add(chunk)); // while endpoint not reached
		// Add checks all the stuff for endpoints and counts

	}

	private IMessage[] fetchHistory(Long last) {
		return RequestBuffer.request(() -> {
			return channel.requestHistory(range.isChronological() ? null : last,
					range.isChronological() ? last : null, MESSAGE_CHUNK_COUNT);
		}).get();
	}

	private boolean add(IMessage[] toAdd) { // Returns true if all messages were added (i.e. end not yet passed)

		if (range.isChronological()) {

			for (int i = toAdd.length - 1; i >= 0; i--) {

				if (range.checkEnd(toAdd[i]) && // check if in range
						(count < 0 || currentCount++ < count)) { // check count
					messages.add(toAdd[i]);
				} else return false;
			}

		} else {

			for (int i = 0; i < toAdd.length; i++) {

				if (range.checkEnd(toAdd[i]) && // check if in range
						(count < 0 || currentCount++ < count)) { // check count
					messages.add(toAdd[i]);
				} else return false; // one of the checks failed, so reached last message
			}
		}

		return true;
	}
}
