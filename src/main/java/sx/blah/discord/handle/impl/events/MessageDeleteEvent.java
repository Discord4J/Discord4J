package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever a message is deleted.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent} instead.
 */
@Deprecated
public class MessageDeleteEvent extends sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent {
	
	public MessageDeleteEvent(IMessage message) {
		super(message);
	}
}
