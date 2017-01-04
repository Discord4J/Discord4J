package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is fired when a message is unpinned from a channel.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.message.MessageUnpinEvent} instead.
 */
@Deprecated
public class MessageUnpinEvent extends sx.blah.discord.handle.impl.events.guild.channel.message.MessageUnpinEvent {
	
	public MessageUnpinEvent(IMessage message) {
		super(message);
	}
}
