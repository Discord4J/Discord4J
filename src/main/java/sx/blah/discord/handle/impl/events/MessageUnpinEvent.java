package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is fired when a message is unpinned from a channel.
 */
public class MessageUnpinEvent extends Event {

	private final IChannel channel;
	private final IMessage message;

	public MessageUnpinEvent(IMessage message) {
		this.message = message;
		this.channel = message.getChannel();
	}

	/**
	 * Gets the channel the message was unpinned from.
	 *
	 * @return The channel.
	 */
	public IChannel getChannel() {
		return channel;
	}

	/**
	 * Gets the message unpinned.
	 *
	 * @return The message.
	 */
	public IMessage getMessage() {
		return message;
	}
}
