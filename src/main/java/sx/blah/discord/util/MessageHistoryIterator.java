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

import org.apache.commons.lang3.ArrayUtils;
import sx.blah.discord.handle.obj.IMessage;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static sx.blah.discord.handle.impl.obj.Channel.MESSAGE_CHUNK_COUNT;

/**
 * This class is an Iterator implementation for channel histories. It fetches messages from a channel's internal cache
 * and requests more whenever needed. When all messages in the provided {@link MessageHistoryRange} have been provided,
 * the iterator assumes a completed state and returns false for hasNext() and null for next(). This class is used as a
 * backing for {@link MessageHistoryBuilder#stream()} and {@link MessageHistoryBuilder#iterator()}.
 *
 * @see MessageHistoryBuilder
 * @see MessageHistoryRange
 */
public class MessageHistoryIterator implements Iterator<IMessage> {

	private final MessageHistoryRange range;

	private IMessage[] backing;
	private int index; // Index of next element to provide
	private RequestBuffer.RequestFuture<IMessage[]> nextCache; // RequestFuture for next chunk

	public MessageHistoryIterator(MessageHistoryRange range) {
		this.range = range;

		IMessage last = range.fetchFirst();

		if (last != null) { // if last is null, there's no history available to get
			backing = range.getChannel().messages.stream().sorted(MessageComparator.REVERSED)
					.collect(Collectors.toList()).toArray(new IMessage[0]);
			index = ArrayUtils.indexOf(backing, last);

			if (index < 0) { // not in cache, request messages
				IMessage[] temp = requestHistory(last.getLongID()).get();
				backing = new IMessage[temp.length + 1];

				// add last to the beginning of the backing (since not included in fetched list)
				index = range.isChronological() ? backing.length - 1 : 0;
				backing[index] = last;
				System.arraycopy(temp, 0, backing, range.isChronological() ? 0 : 1, temp.length);
			}

			nextCache = requestHistory(range.isChronological() ?
					backing[0].getLongID() : backing[backing.length - 1].getLongID()
			);
		}
	}

	@Override
	public boolean hasNext() {
		return backing != null && backing.length != 0 && range.checkEnd(backing[index]);
	}

	@Override
	public IMessage next() {
		if (!hasNext()) {
			if (backing != null) backing = null;
			return null; // no more messages to get
		}

		// next message (backing[index] always guaranteed to be within range)
		IMessage result = backing[range.isChronological() ? index-- : index++];

		if (range.isChronological()) {
			if (index == -1) {
				backing = nextCache.get();
				index = backing.length - 1;
				if (hasNext()) {
					nextCache = requestHistory(backing[0].getLongID());
				}
			}
		} else {
			if (index == backing.length) {
				backing = nextCache.get();
				index = 0;
				if (hasNext()) {
					nextCache = requestHistory(backing[backing.length - 1].getLongID());
				}
			}
		}

		return result;
	}

	public Stream<IMessage> stream() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED), false);
	}

	private RequestBuffer.RequestFuture<IMessage[]> requestHistory(long last) {
		return RequestBuffer.request(() -> {
			return range.getChannel().requestHistory(range.isChronological() ? null : last,
					range.isChronological() ? last : null, MESSAGE_CHUNK_COUNT);
		});
	}
}
