package sx.blah.discord.handle.impl.events.guild.channel.message;

import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever a message is received.
 */
public class MessageReceivedEvent extends MessageEvent {
	
	public MessageReceivedEvent(IMessage message) {
		super(message);
	}
}
