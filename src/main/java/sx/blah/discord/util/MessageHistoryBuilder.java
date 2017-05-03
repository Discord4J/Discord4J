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

import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.objects.MessageObject;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is a utility class for building {@link MessageHistory} objects in a readable, easy-to-understand fashion.
 *
 * @see MessageHistory
 */
public class MessageHistoryBuilder {

	private Channel channel;

	// User provided info
	private IMessage uStart; // Null if not given
	private LocalDateTime uStartTime; // null signifies most recent
	private IMessage uEnd; // Null if not given
	private LocalDateTime uEndTime; // null signifies channel start
	private int count = -1; // < 0 signifies no limit

	// Used for builds
	private List<IMessage> cached;
	private List<IMessage> messages;
	private IMessage start;
	private LocalDateTime end;
	private boolean reversed;
	private int currentCount;

	public MessageHistoryBuilder(IChannel channel) {
		this.channel = (Channel) channel;
	}

	/**
	 * Specifies the message to start at (inclusive).
	 * @param startID the message ID to start at
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder startAt(long startID) {
		this.uStart = channel.getMessageByID(startID);
		if(uStart == null) throw new IllegalArgumentException("Message " + Long.toUnsignedString(startID) + " does not exist in channel " + channel.getStringID() + "!");
		this.uStartTime = uStart.getTimestamp();
		return this;
	}

	/**
	 * Specifies the message to start at (inclusive).
	 * @param msg the message to start at
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder startAt(IMessage msg) {
		if(!channel.equals(msg.getChannel())) throw new IllegalArgumentException("Message " + msg.getStringID() + " does not exist in channel " + channel.getStringID() + "!");
		this.uStart = msg;
		this.uStartTime = uStart.getTimestamp();
		return this;
	}

	/**
	 * Specifies the time to start at (inclusive).
	 * @param startTime the time to start at
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder startAt(LocalDateTime startTime) {
		this.uStart = null;
		this.uStartTime = startTime;
		return this;
	}

	/**
	 * Specifies the message to end at (exclusive).
	 * @param endID the message ID to end at
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder endAt(long endID) {
		this.uEnd = channel.getMessageByID(endID);
		if(uEnd == null) throw new IllegalArgumentException("Message " + Long.toUnsignedString(endID) + " does not exist in channel " + channel.getStringID() + "!");
		this.uEndTime = uEnd.getTimestamp();
		return this;
	}

	public MessageHistoryBuilder endAt(IMessage msg) {
		if(!channel.equals(msg.getChannel())) throw new IllegalArgumentException("Message " + msg.getStringID() + " does not exist in channel " + channel.getStringID() + "!");
		this.uEnd = msg;
		this.uEndTime = msg.getTimestamp();
		return this;
	}

	/**
	 * Specifies the time to end at (exclusive).
	 * @param endTime the time to end at
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder endAt(LocalDateTime endTime) {
		this.uEnd = null;
		this.uEndTime = endTime;
		return this;
	}

	/**
	 * Specifies the maximum number of messages to get. Defaults to -1 (no limit).
	 * @param count max number of messages to get
	 * @return this, for chaining
	 */
	public MessageHistoryBuilder withMaxCount(int count) {
		if(count < 1) throw new IllegalArgumentException();
		this.count = count;
		return this;
	}

	public MessageHistory build() {

		// Get cache from the channel, sorted
		cached = channel.messages.stream().sorted(MessageComparator.DEFAULT).collect(Collectors.toList());

		// Initialize messages
		messages = count < 0 ?
				new ArrayList<>(MESSAGE_CHUNK_COUNT) :
				new ArrayList<>(count);

		reversed = uStartTime != null && uEndTime != null && uStartTime.isBefore(uEndTime); // normal messagehistory order is latest to earliest

		if(reversed && count < 0) {

			reversed = false;

			start = uEnd == null ? fetchStart(uEndTime) : uEnd;

			end = uStartTime == null ?
					uStart == null ?
							null :
							uStart.getTimestamp() :
					uStartTime;

			fetch();

			Collections.reverse(messages);

		} else {

			start = uStart == null ? fetchStart(uStartTime) : uStart;

			end = uEndTime == null ?
					uEnd == null ?
							null :
							uEnd.getTimestamp() :
					uEndTime;

			fetch();
		}

		return new MessageHistory(messages);
	}

	// Called when: not reversed, count limited, end specified
	private void fetch() {

		if(start == null) {
			messages = new ArrayList<>(); // Means out of range start
			return;
		}

		messages.add(start); // Add first message (first message of each chunk not added)

		IMessage last = start;

		// Get a chunk of messages
		IMessage[] chunk;

		do { // add messages to list, returns whether or not the endpoint was reached
			// Get a chunk
			chunk = requestHistory(reversed ? null : last.getLongID(),
					reversed ? last.getLongID() : null, MESSAGE_CHUNK_COUNT);

			// get new starting point- last message of list for normal, first message if reversed
			last = chunk[reversed ? 0 : chunk.length - 1];
		} while(add(chunk)); // while endpoint not reached
		// Add checks all the stuff for endpoints and counts

	}

	// Called when: reversed, count limited, end specified
	private IMessage[] fetchReversed(IMessage start, LocalDateTime startTime, LocalDateTime endTime, int count) {

		List<IMessage> cached = channel.messages.stream().sorted(MessageComparator.DEFAULT).collect(Collectors.toList());

		IMessage last = start == null ? fetchStart(cached, true, startTime) : start;

		if(last == null) return new IMessage[0]; // Means out of range start

		IMessage[] result = new IMessage[count];
		int index = 0;

		IMessage[] chunk = requestHistory(null, last.getLongID(), Math.min(count+1, MESSAGE_CHUNK_COUNT));

		while(count > 0 && chunk.length > 0 &&
				checkEnd(chunk[0], endTime, true)) { // check if all within end
			copyReverse(chunk, 0, result, index, chunk.length - 1); // Append everything up to last
			last = chunk[0];
			chunk = requestHistory(null, last.getLongID(), Math.min(count+1, MESSAGE_CHUNK_COUNT));
		}

		if(chunk.length == 0 || count == 0) return result; // already got all elements needed or able to, return result

		int stop = 0;
		while(checkEnd(chunk[chunk.length - 1 - stop], endTime, true) && stop < count) stop++; // find last element before end

		return appendReverse(result, chunk, 1, stop);
	}

	private IMessage fetchStart(LocalDateTime startTime) {

		// Check the cache for the start message
		if(reversed) {
			for (int i = 0; i < cached.size(); i++) {
				if (!checkStart(cached.get(i), startTime, reversed)) // if we just went past start message (confusing logic)
					return i == 0 ? null : cached.get(i - 1);
			}
		} else {
			for (int i = 0; i < cached.size(); i++) {
				if (checkStart(cached.get(i), startTime, reversed)) // if we just went past start message (confusing logic)
					return cached.get(i);
			}
		}

		// If execution has reached this point, need to request history to find message
		IMessage lastMessage = cached.get(cached.size() - 1); // start at last element of cached stuff

		// Get chunk of messages
		IMessage[] chunk = requestHistory(lastMessage.getLongID(), null, MESSAGE_CHUNK_COUNT);

		// While we haven't reached the channel's end
		while(chunk.length != 0) {

			// Same logic as for cache
			if(reversed) {
				for (int i = 0; i < chunk.length; i++) {
					if (!checkStart(chunk[i], startTime, reversed)) // if we just went past start message (confusing logic)
						return i == 0 ? null : chunk[i - 1];
				}
			} else {
				for (int i = 0; i < cached.size(); i++) {
					if (checkStart(chunk[i], startTime, reversed)) // if we just went past start message (confusing logic)
						return chunk[i];
				}
			}

			lastMessage = chunk[chunk.length - 1];
			chunk = requestHistory(lastMessage.getLongID(), null, MESSAGE_CHUNK_COUNT);
		}

		return null;
	}

	private IMessage[] requestHistory(Long before, Long after, int limit) {
		DiscordUtils.checkPermissions(channel.getClient(), channel, EnumSet.of(Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY));

		String queryParams = "?limit=" + limit;

		if (before != null) {
			queryParams += "&before=" + Long.toUnsignedString(before);
		} else if (after != null) {
			queryParams += "&after=" + Long.toUnsignedString(after);
		}

		final String params = queryParams;

		MessageObject[] messages = RequestBuffer.request(() -> {
				return ((DiscordClientImpl) channel.getClient())
					.REQUESTS.GET.makeRequest(
						DiscordEndpoints.CHANNELS + channel.getStringID() + "/messages" + params,
						MessageObject[].class);}).get();

		if (messages.length == 0) {
			return new IMessage[0];
		}

		IMessage[] messageObjs = new IMessage[messages.length];

		for (int i = 0; i < messages.length; i++) {
			messageObjs[i] = DiscordUtils.getMessageFromJSON(channel, messages[i]);
		}

		return messageObjs;
	}

	private boolean checkStart(IMessage msg, LocalDateTime startTime, boolean reversed) {
		return reversed?
				startTime.compareTo(msg.getTimestamp()) <= 0:
				startTime.compareTo(msg.getTimestamp()) >= 0;
	}

	private boolean checkEnd(IMessage msg) {
		return reversed?
				end.isAfter(msg.getTimestamp()) :
				end.isBefore(msg.getTimestamp());
	}

	private boolean add(IMessage[] toAdd) { // Returns true if all messages were added (i.e. end not yet passed)

		if(reversed) {
			for(int i = toAdd.length-1; i > 0; i--) {
				if(checkEnd(toAdd[i]) && count < 0 || currentCount < count) {
					messages.add(toAdd[i]);
				} else return false;
			}
		} else {
			for(int i = 1; i < toAdd.length; i++) {
				if(checkEnd(toAdd[i]) && count < 0 || currentCount < count) {
					messages.add(toAdd[i]);
				} else return false;
			}
		}

		return true;
	}
}
