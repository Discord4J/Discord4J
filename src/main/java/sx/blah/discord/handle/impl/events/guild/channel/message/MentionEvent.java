package sx.blah.discord.handle.impl.events.guild.channel.message;

import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever the bot is @mentioned.
 */
public class MentionEvent extends MessageEvent {
	
	public MentionEvent(IMessage message) {
		super(message);
	}
}
