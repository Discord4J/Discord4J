package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched if a user is typing.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.TypingEvent} instead.
 */
@Deprecated
public class TypingEvent extends sx.blah.discord.handle.impl.events.guild.channel.TypingEvent {
	
	public TypingEvent(IUser user, IChannel channel) {
		super(user, channel);
	}
}
