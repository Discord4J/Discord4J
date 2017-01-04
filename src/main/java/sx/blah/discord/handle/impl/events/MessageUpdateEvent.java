package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever a message is edited.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.message.MessageUpdateEvent} instead.
 */
@Deprecated
public class MessageUpdateEvent extends sx.blah.discord.handle.impl.events.guild.channel.message.MessageUpdateEvent {
	
	public MessageUpdateEvent(IMessage oldMessage, IMessage newMessage) {
		super(oldMessage, newMessage);
	}
}
