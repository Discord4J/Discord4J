package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever a message is edited.
 */
public class MessageUpdateEvent extends Event {

	private final IMessage oldMessage, newMessage;

	public MessageUpdateEvent(IMessage oldMessage, IMessage newMessage) {
		this.oldMessage = oldMessage;
		this.newMessage = newMessage;
	}

	/**
	 * The original message.
	 *
	 * @return The message.
	 */
	public IMessage getOldMessage() {
		return oldMessage;
	}

	/**
	 * The new message.
	 *
	 * @return The message.
	 */
	public IMessage getNewMessage() {
		return newMessage;
	}
}
