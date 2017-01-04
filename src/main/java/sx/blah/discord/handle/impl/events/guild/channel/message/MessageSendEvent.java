package sx.blah.discord.handle.impl.events.guild.channel.message;

import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever a message is sent by the bot.
 */
public class MessageSendEvent extends MessageEvent {
	
	public MessageSendEvent(IMessage message) {
		super(message);
	}
}
