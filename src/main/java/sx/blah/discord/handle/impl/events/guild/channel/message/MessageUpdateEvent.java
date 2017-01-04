package sx.blah.discord.handle.impl.events.guild.channel.message;

import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever a message is edited.
 */
public class MessageUpdateEvent extends MessageEvent {

	private final IMessage oldMessage, newMessage;

	public MessageUpdateEvent(IMessage oldMessage, IMessage newMessage) {
		super(newMessage);
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
