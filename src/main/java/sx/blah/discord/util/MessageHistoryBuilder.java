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
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;

import java.time.LocalDateTime;
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

	public MessageHistoryBuilder(Channel channel) {
		this.channel = channel;
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

		boolean reversed = uStartTime.isBefore(uEndTime); // normal messagehistory order is latest to earliest

		return new MessageHistory();
	}

	private static IMessage[] append(IMessage[] orig, IMessage[] add, int start, int count) {
		IMessage[] result = new IMessage[orig.length + count];
		System.arraycopy(orig, 0, result, 0, orig.length);
		System.arraycopy(add, start, result, orig.length, count);
		return result;
	}

	private static IMessage[] appendReverse(IMessage[] orig, IMessage[] add, int start, int count) {
		IMessage[] result = new IMessage[orig.length + count];
		System.arraycopy(orig, 0, result, 0, orig.length);
		copyReverse(result, orig.length, add, start, count);
		return result;
	}

	private static void copyReverse(IMessage[] src, int srcStart, IMessage[] dest, int destStart, int count) {
		srcStart = src.length - 1 - srcStart;
		for(; count > 0; count--) {
			dest[destStart] = src[srcStart];
			destStart++;
			srcStart--;
		}
	}
}
