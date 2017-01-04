package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;

/**
 * Fired when a reaction gets added to a message.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent} instead.
 */
@Deprecated
public class ReactionAddEvent extends sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent {
	
	public ReactionAddEvent(IMessage message, IReaction reaction, IUser user) {
		super(message, reaction, user);
	}
}
