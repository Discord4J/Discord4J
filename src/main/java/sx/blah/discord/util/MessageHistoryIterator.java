package sx.blah.discord.util;

import sx.blah.discord.handle.obj.IMessage;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MessageHistoryIterator implements Iterator<IMessage> {

	private MessageHistoryRange range;

	private IMessage[] backing;
	private int nextIndex;
	private RequestBuffer.RequestFuture<IMessage[]> nextCache;

	public MessageHistoryIterator(MessageHistoryRange range) {
		this.range = range;
		backing = (IMessage[]) range.getChannel().messages.values().toArray();
		nextIndex = 0;

		nextCache = RequestBuffer.request(() -> {return range.getChannel().requestHistory(null, null,  0);});
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public IMessage next() {
		return null;
	}

	public Stream<IMessage> stream() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED), false);
	}
}
