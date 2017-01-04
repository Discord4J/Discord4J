package sx.blah.discord.handle.impl.events.guild.channel.message;

import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is fired when a message is unpinned from a channel.
 */
public class MessageUnpinEvent extends MessageEvent {
	
	public MessageUnpinEvent(IMessage message) {
		super(message);
	}
}
