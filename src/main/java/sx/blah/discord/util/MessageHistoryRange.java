package sx.blah.discord.util;

import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MessageHistoryRange {

	private Channel channel;

	private Endpoint start;
	private Endpoint end;

	private boolean chronological;

	public MessageHistoryRange(IChannel channel, Endpoint start, Endpoint end) {
		this.channel = (Channel) channel;

		this.start = start;
		this.end = end;

		chronological = start.isBefore(end);
	}

	public Channel getChannel() {
		return channel;
	}

	public Endpoint getStart() {
		return start;
	}

	public Endpoint getEnd() {
		return end;
	}

	public boolean isChronological() {
		return chronological;
	}

	public IMessage fetchFirstInRange() {

		if(start.getMessage() != null) return start.getMessage();

		List<IMessage> cached = channel.messages.stream().sorted(MessageComparator.DEFAULT).collect(Collectors.toList());

		// Check the cache for the start message
		if(chronological) {
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
		IMessage[] chunk = RequestBuffer.request(() -> {return channel.requestHistory(fLast, null, Channel.MESSAGE_CHUNK_COUNT);}).get();

		// While we haven't reached the channel's end
		while(chunk.length != 0) {

			// Same logic as for cache
			if(chronological) {
				for (int i = 0; i < chunk.length; i++) {
					if (!checkStart(chunk[i])) // if we just went past start message (confusing logic)
						return i == 0 ? null : chunk[i - 1];
				}
			} else {
				for (int i = 0; i < cached.size(); i++) {
					if (checkStart(chunk[i])) // if we just hit start message (confusing logic)
						return chunk[i];
				}
			}

			last = chunk[chunk.length - 1].getLongID();
			long lastF = last;
			chunk = RequestBuffer.request(() -> {return channel.requestHistory(lastF, null, Channel.MESSAGE_CHUNK_COUNT);}).get();
		}

		return null;
	}

	public boolean checkStart(IMessage msg) {
		return chronological ?
				start != Endpoint.NOW &&
						msg.getTimestamp().compareTo(start.getTime()) > (start.inclusive ? -1 : 0) :
				start != Endpoint.CHANNEL_CREATE &&
						msg.getTimestamp().compareTo(start.getTime()) < (start.inclusive ? 1 : 0);
	}

	public boolean checkEnd(IMessage msg) {
		return chronological ?
				end != Endpoint.CHANNEL_CREATE &&
						msg.getTimestamp().compareTo(end.getTime()) < (start.inclusive ? 1 : 0) :
				end != Endpoint.NOW &&
						msg.getTimestamp().compareTo(end.getTime()) > (start.inclusive ? -1 : 0);
	}

	public static class Endpoint {
		public static final Endpoint CHANNEL_CREATE = new Endpoint();
		public static final Endpoint NOW = new Endpoint();

		private IMessage msg;
		private LocalDateTime time;
		private boolean inclusive;

		private Endpoint() {}

		public Endpoint(IMessage msg, boolean include) {
			this.msg = msg;
			this.inclusive = include;
		}

		public Endpoint(LocalDateTime time, boolean include) {
			this.time = time;
			this.inclusive = include;
		}

		public IMessage getMessage() {
			return msg;
		}

		public LocalDateTime getTime() {
			if(this == CHANNEL_CREATE) return LocalDateTime.MIN;
			if (this == NOW) return LocalDateTime.MAX;
			return time == null ? msg.getTimestamp() : time;
		}

		public boolean isIncluded() {
			return inclusive;
		}

		public boolean isBefore(Endpoint other) {
			return this.getTime().isBefore(other.getTime());
		}

		public boolean isAfter(Endpoint other) {
			return this.getTime().isAfter(other.getTime());
		}
	}
}
