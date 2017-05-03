package sx.blah.discord.util;

import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

import java.time.LocalDateTime;

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

	public IMessage fetchFirstInRange() {
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
