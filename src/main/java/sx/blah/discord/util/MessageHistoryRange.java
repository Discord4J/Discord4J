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
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a range of a channel's message history. It includes a start and endpoint to clearly define the
 * range of messages. These endpoints can be either timestamps or messages, and can be inclusive or exclusive. This
 * class also contains some logic regarding getting the first message of the range (the message closest to `start` that
 * is in range).
 * It is used in {@link MessageHistoryBuilder} and {@link MessageHistoryIterator} to define the range being requested.
 *
 * @see MessageHistoryBuilder
 * @see MessageHistoryIterator
 * @see Point
 */
public class MessageHistoryRange {

	private final Channel channel;

	private final Point start;
	private final Point end;

	private final boolean chronological;

	public MessageHistoryRange(IChannel channel, Point start, Point end) {
		this.channel = (Channel) channel;

		this.start = start;
		this.end = end;

		chronological = start.isBefore(end);
	}

	/**
	 * Gets the channel this range is defined in.
	 *
	 * @return the channel of this range
	 */
	public Channel getChannel() {
		return channel;
	}

	/**
	 * Gets the {@link Point} object that defines this range's starting point
	 *
	 * @return the starting point
	 */
	public Point getStart() {
		return start;
	}

	/**
	 * Gets the {@link Point} object that defines this range's ending point
	 *
	 * @return the ending point
	 */
	public Point getEnd() {
		return end;
	}

	/**
	 * Returns true if the starting point is chronologically before the ending point.
	 * NOTE: A false value from this method corresponds to the way message histories are viewed in Discord, starting at
	 * the most recent message and ending at the beginning of the channel's history. A true value from this method would
	 * be the opposite.
	 *
	 * @return whether this range is chronological
	 */
	public boolean isChronological() {
		return chronological;
	}

	/**
	 * Gets the first message considered "in range" for this range. The first message is the one closest to `start`.
	 * Equivalent to `#getStart().getMessage()` if a message was supplied as the starting point.
	 * NOTE: This method accounts for the starting point's inclusivity.
	 *
	 * @return the first message within this range
	 */
	public IMessage fetchFirst() {

		if (start.getMessage() != null && start.inclusive) return start.getMessage();

		List<IMessage> cached = channel.messages.stream().sorted(MessageComparator.REVERSED).collect(Collectors.toList());

		// Check the cache for the start message
		if (chronological) {
			for (int i = 0; i < cached.size(); i++) {
				if (!checkStart(cached.get(i))) // if we just went past start message (confusing logic)
					return i == 0 ? null : cached.get(i - 1);
			}
		} else {
			for (int i = 0; i < cached.size(); i++) {
				if (checkStart(cached.get(i))) // if we just hit start message (confusing logic)
					return cached.get(i);
			}
		}

		// If execution has reached this point, need to request history to find message
		long last = cached.isEmpty() ? 0 : cached.get(cached.size() - 1).getLongID(); // start at last element of cached stuff

		// Get chunk of messages
		long fLast = last;
		IMessage[] chunk = RequestBuffer.request(() -> {
			return channel.requestHistory(fLast, null, Channel.MESSAGE_CHUNK_COUNT);
		}).get();

		// While we haven't reached the channel's end
		while (chunk.length != 0) {

			// Same logic as for cache
			if (chronological) {
				for (int i = 0; i < chunk.length; i++) {
					if (!checkStart(chunk[i])) // if we just went past start message (confusing logic)
						return i == 0 ? null : chunk[i - 1];
				}
			} else {
				for (int i = 0; i < chunk.length; i++) {
					if (checkStart(chunk[i])) // if we just hit start message (confusing logic)
						return chunk[i];
				}
			}

			last = chunk[chunk.length - 1].getLongID();
			long lastF = last;
			chunk = RequestBuffer.request(() -> {
				return channel.requestHistory(lastF, null, Channel.MESSAGE_CHUNK_COUNT);
			}).get();
		}

		return null;
	}

	/**
	 * Checks whether a message is beyond the starting point.
	 * NOTE: This method accounts for the direction of `end` relative to `start`.
	 *
	 * @param msg the message to check
	 * @return whether the message is past the starting point.
	 */
	public boolean checkStart(IMessage msg) {
		return chronological ?
				start != Point.NOW &&
						msg.getTimestamp().compareTo(start.getTime()) > (start.inclusive ? -1 : 0) :
				start != Point.CHANNEL_CREATE &&
						msg.getTimestamp().compareTo(start.getTime()) < (start.inclusive ? 1 : 0);
	}

	/**
	 * Checks whether a message is before the ending point.
	 * NOTE: This method accounts for the direction of `end` relative to `start`.
	 *
	 * @param msg the message to check
	 * @return whether the message is past the ending point.
	 */
	public boolean checkEnd(IMessage msg) {
		return chronological ?
				end != Point.CHANNEL_CREATE &&
						msg.getTimestamp().compareTo(end.getTime()) < (end.inclusive ? 1 : 0) :
				end != Point.NOW &&
						msg.getTimestamp().compareTo(end.getTime()) > (end.inclusive ? -1 : 0);
	}

	/**
	 * This class represents an ending point for a {@link MessageHistoryRange}. It can be defined as either a timestamp
	 * or a message, and includes whether or not the point should be included in the range. This class also includes
	 * constants for the idea of a channel's beginning and a channel's most recent point.
	 */
	public static class Point {
		/**
		 * This constant represents the channel's earliest point, its creation. Used as default ending point for {@link MessageHistoryBuilder}.
		 * Essentially equivalent to the oldest message in the channel, INCLUSIVE.
		 */
		public static final Point CHANNEL_CREATE = new Point();

		/**
		 * This constant represents the channel's most recent point. Used as default starting point for {@link MessageHistoryBuilder}.
		 * Essentially equivalent to the most recent message in the channel, INCLUSIVE.
		 */
		public static final Point NOW = new Point();

		private final IMessage msg;
		private final LocalDateTime time;
		private final boolean inclusive;

		// Used for the two constants
		private Point() {
			msg = null;
			time = null;
			inclusive = false;
		}

		public Point(IMessage msg, boolean include) {
			if (msg == null) throw new IllegalArgumentException("Message argument for endpoint cannot be null!");
			this.msg = msg;
			this.time = null;
			this.inclusive = include;
		}

		public Point(LocalDateTime time, boolean include) {
			if (time == null) throw new IllegalArgumentException("Time argument for endpoint cannot be null!");
			this.time = time;
			this.msg = null;
			this.inclusive = include;
		}

		/**
		 * Gets the message this endpoint is defined as. Returns null if the endpoint was defined using a timestamp.
		 *
		 * @return the message, or null if nonexistent
		 */
		public IMessage getMessage() {
			return msg;
		}

		/**
		 * Gets the timestamp this endpoint is defined as. Returns the message's timestamp if the endpoint was defined
		 * using a message.
		 *
		 * @return the timestamp
		 */
		public LocalDateTime getTime() {
			if (this == CHANNEL_CREATE) return LocalDateTime.MIN;
			if (this == NOW) return LocalDateTime.MAX;
			return time == null ? msg.getTimestamp() : time;
		}

		/**
		 * Returns true if this point is included in the range.
		 *
		 * @return whether the point is included
		 */
		public boolean isIncluded() {
			return inclusive;
		}

		/**
		 * Compares this endpoint to another, returning true if this point is chronologically prior to other.
		 *
		 * @param other the endpoint to compare to
		 * @return true if this endpoint is before other
		 */
		public boolean isBefore(Point other) {
			return this.getTime().isBefore(other.getTime());
		}

		/**
		 * Compares this endpoint to another, returning true if this point is chronologically after other.
		 *
		 * @param other the endpoint to compare to
		 * @return true if this endpoint is after other
		 */
		public boolean isAfter(Point other) {
			return this.getTime().isAfter(other.getTime());
		}
	}
}
