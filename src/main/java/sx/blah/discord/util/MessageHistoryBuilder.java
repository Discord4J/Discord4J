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
import java.util.concurrent.CompletableFuture;
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

	private final Channel channel;

	// User provided info
	private IMessage start; // Null if not given
	private LocalDateTime startTime; // null signifies most recent
	private boolean includeStart;
	private LocalDateTime endTime; // null signifies channel start
	private boolean includeEnd;
	private int limit = -1; // < 0 signifies no limit
	private boolean lenient;

	// Used for builds
	private List<IMessage> cached;
	private List<IMessage> messages;
	private MessageHistoryRange range;
	private int count;

	public MessageHistoryBuilder(IChannel channel) {
		this.channel = (Channel) channel;
	}

	/**
	 * Determines whether to throw exceptions on invalid inputs. If true, invalid input is ignored. Default false.
	 *
	 * @param lenient the lenience
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder setLenient(boolean lenient) {
		this.lenient = lenient;
		return this;
	}

	/**
	 * Specifies the message to start at.
	 *
	 * @param startID the message ID to start at (inclusive)
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder startAt(long startID) {
		return startAt(channel.getMessageByID(startID));
	}

	/**
	 * Specifies the message to start at.
	 *
	 * @param msg the message to start at (inclusive)
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder startAt(IMessage msg) {
		return startAt(msg, true);
	}

	/**
	 * Specifies the time to start at.
	 * NOTE: Using this method to set a start time chronologically before endAt, and using withMaxCount > 0, may
	 * result in SIGNIFICANT performance loss.
	 *
	 * @param startTime the time to start at (inclusive)
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder startAt(LocalDateTime startTime) {
		return startAt(startTime, true);
	}

	/**
	 * Specifies the message to start at.
	 *
	 * @param startID   the message ID to start at
	 * @param inclusive whether or not to include the specified message
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder startAt(long startID, boolean inclusive) {
		return startAt(channel.getMessageByID(startID), inclusive);
	}

	/**
	 * Specifies the message to start at.
	 *
	 * @param msg       the message to start at
	 * @param inclusive whether or not to include the specified message
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder startAt(IMessage msg, boolean inclusive) {
		if (msg == null || !channel.equals(msg.getChannel())) {
			if (lenient) return this; // Ignores input values
			throw new IllegalArgumentException("Message is null or does not exist in channel " + channel.getStringID() + "!");
		}
		this.start = msg;
		this.startTime = start.getTimestamp();
		this.includeStart = inclusive;
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
		if (startTime == null) {
			if (lenient) return this; // Ignores input values
			throw new IllegalArgumentException("Start time is null!");
		}
		this.start = null;
		this.startTime = startTime;
		this.includeStart = inclusive;
		return this;
	}

	/**
	 * Specifies the message to end at.
	 *
	 * @param endID the message ID to end at (exclusive)
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder endAt(long endID) {
		return endAt(channel.getMessageByID(endID));
	}

	/**
	 * Specifies the message to end at.
	 *
	 * @param msg the message ID to end at (exclusive)
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder endAt(IMessage msg) {
		return endAt(msg, false);
	}

	/**
	 * Specifies the time to end at.
	 *
	 * @param endTime the time to end at (exclusive)
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder endAt(LocalDateTime endTime) {
		return endAt(endTime, false);
	}

	/**
	 * Specifies the message to end at.
	 *
	 * @param endID     the message ID to end at
	 * @param inclusive whether or not to include this time
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder endAt(long endID, boolean inclusive) {
		return endAt(channel.getMessageByID(endID), inclusive);
	}

	/**
	 * Specifies the message to end at.
	 *
	 * @param msg       the message ID to end at
	 * @param inclusive whether or not to include this time
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder endAt(IMessage msg, boolean inclusive) {
		if (msg == null || !channel.equals(msg.getChannel())) {
			if (lenient) return this;
			throw new IllegalArgumentException("Message is null or does not exist in channel " + channel.getStringID() + "!");
		}
		this.endTime = msg.getTimestamp();
		this.includeEnd = inclusive;
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
		if (endTime == null) {
			if (lenient) return this;
			throw new IllegalArgumentException("End time is null!");
		}
		this.endTime = endTime;
		this.includeEnd = inclusive;
		return this;
	}

	/**
	 * Specifies the maximum number of messages to get. Defaults to -1 (no limit).
	 *
	 * @param limit max number of messages to get
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder withMaxCount(int limit) {
		if (limit < 0 && !lenient) throw new IllegalArgumentException("Limit is negative!");
		this.limit = limit;
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
	 * NOTE: This object can be put directly in a for-each loop without using this method.
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
	public CompletableFuture<MessageHistory> request() {
		return CompletableFuture.supplyAsync(this::get);
	}

	/**
	 * Builds a {@link MessageHistory} object with the parameters set in this builder.
	 * NOTE: This will block the thread until all messages are retrieved. Use {@link MessageHistoryBuilder#request()}
	 * to request this message history without blocking.
	 *
	 * @return the message history
	 * @see MessageHistory
	 */
	public MessageHistory get() {
		if (limit == 0) {
			messages = new ArrayList<>(); // Empty array list because limit = 0

		} else {
			// Get cache from the channel, sorted
			cached = channel.messages.stream().sorted(MessageComparator.REVERSED).collect(Collectors.toList());

			buildRange();

			// Initialize messages
			messages = limit < 0 ?
					new ArrayList<>(MESSAGE_CHUNK_COUNT) :
					new ArrayList<>(limit);

			// Checks for special case for efficiency- if a range is reverse ordered (start earlier in history than end) but
			// count unlimited, then it is equivalent to switching endpoints and reversing the collection afterwards
			if (range.isChronological() && limit < 0) {
				range = new MessageHistoryRange(channel,
						new MessageHistoryRange.Endpoint(endTime, includeEnd),
						start == null ?
								new MessageHistoryRange.Endpoint(startTime, includeStart) :
								new MessageHistoryRange.Endpoint(start, includeStart)
				);

				fetch();
				Collections.reverse(messages);
			} else {
				fetch();
			}
		}

		return new MessageHistory(messages);
	}

	private void buildRange() {
		MessageHistoryRange.Endpoint first;
		MessageHistoryRange.Endpoint second;

		if (start != null) { // if start message was specified
			first = new MessageHistoryRange.Endpoint(start, includeStart);
		} else {
			if (startTime != null) { // if start time was specified
				first = new MessageHistoryRange.Endpoint(startTime, includeStart);
			} else {
				first = MessageHistoryRange.Endpoint.NOW; // No start time given. Defaults to most recent message.
			}
		}

		if (endTime != null) { // if end time was specified
			second = new MessageHistoryRange.Endpoint(endTime, includeEnd);
		} else {
			second = MessageHistoryRange.Endpoint.CHANNEL_CREATE; // No end time given. Defaults to channel create.
		}

		range = new MessageHistoryRange(channel, first, second);
	}

	private void fetch() {
		// fetch first message from the range
		IMessage last = range.fetchFirstInRange();

		if (last == null) { // Means out of range start
			messages = new ArrayList<>();
			return;
		}

		messages.add(last); // Add first message

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

			// Add checks all the stuff for endpoints and limits
		} while (add(chunk)); // while endpoint not reached

	}

	private IMessage[] fetchHistory(long last) {
		return RequestBuffer.request(() -> {
			return channel.requestHistory(range.isChronological() ? null : last,
					range.isChronological() ? last : null, MESSAGE_CHUNK_COUNT);
		}).get();
	}

	// Returns true if all messages in toAdd were added (i.e. end wasn't reached)
	private boolean add(IMessage[] toAdd) {
		if (range.isChronological()) {
			for (int i = toAdd.length - 1; i >= 0; i--) {
				if (range.checkEnd(toAdd[i]) && // check if in range
						(limit < 0 || count < limit)) { // check count
					messages.add(toAdd[i]);
					count++;
				} else return false;
			}
		} else {
			for (int i = 0; i < toAdd.length; i++) {
				if (range.checkEnd(toAdd[i]) && // check if in range
						(limit < 0 || count++ < limit)) { // check count
					messages.add(toAdd[i]);
					count++;
				} else return false; // one of the checks failed, so reached last message
			}
		}

		return true;
	}
}
