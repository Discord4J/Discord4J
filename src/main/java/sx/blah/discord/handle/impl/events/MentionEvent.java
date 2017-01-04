package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched whenever the bot is @mentioned.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent} instead.
 */
@Deprecated
public class MentionEvent extends sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent {
	
	public MentionEvent(IMessage message) {
		super(message);
	}
}
