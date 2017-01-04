package sx.blah.discord.handle.impl.events.guild.channel.message;

import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is fired when a message is pinned to a channel.
 */
public class MessagePinEvent extends MessageEvent {
	
	public MessagePinEvent(IMessage message) {
		super(message);
	}
}
