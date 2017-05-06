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

public class MessageHistoryIterator implements Iterable<IMessage>, Iterator<IMessage> {

	private MessageHistoryRange range;

	private IMessage[] backing;
	private int index; // Index of next element to provide
	private RequestBuffer.RequestFuture<IMessage[]> nextCache; // RequestFuture for next chunk

	public MessageHistoryIterator(MessageHistoryRange range) {
		this.range = range;

		IMessage last = range.fetchFirstInRange();

		if (last != null) { // if last is null, there's no history available to get

			backing = range.getChannel().messages.stream().sorted(MessageComparator.REVERSED).collect(Collectors.toList()).toArray(new IMessage[0]);

			index = ArrayUtils.indexOf(backing, last);

			if (index < 0) {
				backing = requestHistory(last.getLongID()).get();
				index = range.isChronological() ? backing.length - 1 : 0;
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
			if(backing != null) backing = null;
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

	@Override
	public Iterator<IMessage> iterator() {
		return this;
	}

	public Stream<IMessage> stream() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED), false);
	}

	private RequestBuffer.RequestFuture<IMessage[]> requestHistory(Long last) {
		return RequestBuffer.request(() -> {
			return range.getChannel().requestHistory(range.isChronological() ? null : last,
					range.isChronological() ? last : null, MESSAGE_CHUNK_COUNT);
		});
	}
}
