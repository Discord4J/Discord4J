package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever a message is sent by the bot.
 */
public class MessageSendEvent extends Event {

	private IMessage message;

	public MessageSendEvent(IMessage message) {
		this.message = message;
	}

	/**
	 * Gets the message sent.
	 *
	 * @return The message.
	 */
	public IMessage getMessage() {
		return message;
	}
}
