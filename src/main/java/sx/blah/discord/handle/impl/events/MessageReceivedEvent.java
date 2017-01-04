package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever a message is received.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent} instead.
 */
@Deprecated
public class MessageReceivedEvent extends sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent {
	
	public MessageReceivedEvent(IMessage message) {
		super(message);
	}
}
