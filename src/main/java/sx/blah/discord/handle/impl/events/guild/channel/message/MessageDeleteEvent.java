package sx.blah.discord.handle.impl.events.guild.channel.message;

import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever a message is deleted.
 */
public class MessageDeleteEvent extends MessageEvent {
	
	public MessageDeleteEvent(IMessage message) {
		super(message);
	}
}
