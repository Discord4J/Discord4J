package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever a message is deleted.
 */
public class MessageDeleteEvent extends Event {

	private final IMessage message;

	public MessageDeleteEvent(IMessage message) {
		this.message = message;
	}

	/**
	 * Gets the message deleted.
	 *
	 * @return The message.
	 */
	public IMessage getMessage() {
		return message;
	}
}
