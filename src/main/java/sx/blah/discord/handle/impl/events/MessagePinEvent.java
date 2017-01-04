package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is fired when a message is pinned to a channel.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.message.MessagePinEvent} instead.
 */
@Deprecated
public class MessagePinEvent extends sx.blah.discord.handle.impl.events.guild.channel.message.MessagePinEvent {
	
	public MessagePinEvent(IMessage message) {
		super(message);
	}
}
