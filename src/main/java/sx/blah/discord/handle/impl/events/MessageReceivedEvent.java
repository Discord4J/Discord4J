package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever a message is received.
 */
public class MessageReceivedEvent extends Event {

	private final IMessage message;

	public MessageReceivedEvent(IMessage message) {
		this.message = message;
	}

	/**
	 * Gets the message received.
	 *
	 * @return The message.
	 */
	public IMessage getMessage() {
		return message;
	}
}
