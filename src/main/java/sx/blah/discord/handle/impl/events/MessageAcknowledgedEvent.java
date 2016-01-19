package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched when this bot's account acknowledges a message.
 */
public class MessageAcknowledgedEvent extends Event {
	
	private final IMessage acknowledged;
	
	public MessageAcknowledgedEvent(IMessage acknowledged) {
		this.acknowledged = acknowledged;
	}
	
	/**
	 * Gets the message that was acknowledged.
	 *
	 * @return The acknowledged message.
	 */
	public IMessage getAcknowledgedMessage() {
		return acknowledged;
	}
}
