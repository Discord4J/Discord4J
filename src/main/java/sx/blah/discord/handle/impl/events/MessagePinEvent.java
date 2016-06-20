package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is fired when a message is pinned to a channel.
 */
public class MessagePinEvent extends Event {

	private final IChannel channel;
	private final IMessage message;

	public MessagePinEvent(IMessage message) {
		this.message = message;
		this.channel = message.getChannel();
	}

	/**
	 * Gets the channel the message was pinned to.
	 *
	 * @return The channel.
	 */
	public IChannel getChannel() {
		return channel;
	}

	/**
	 * Gets the message pinned.
	 *
	 * @return The message.
	 */
	public IMessage getMessage() {
		return message;
	}
}
