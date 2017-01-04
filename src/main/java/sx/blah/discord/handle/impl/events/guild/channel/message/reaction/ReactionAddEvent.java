package sx.blah.discord.handle.impl.events.guild.channel.message.reaction;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;

/**
 * Fired when a reaction gets added to a message.
 */
public class ReactionAddEvent extends ReactionEvent {
	
	public ReactionAddEvent(IMessage message, IReaction reaction, IUser user) {
		super(message, reaction, user);
	}
}
