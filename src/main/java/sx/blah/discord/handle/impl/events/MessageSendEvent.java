package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever a message is sent by the bot.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.message.MessageSendEvent} instead.
 */
@Deprecated
public class MessageSendEvent extends sx.blah.discord.handle.impl.events.guild.channel.message.MessageSendEvent {
	
	public MessageSendEvent(IMessage message) {
		super(message);
	}
}
