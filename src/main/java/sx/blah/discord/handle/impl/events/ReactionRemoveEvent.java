package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;

/**
 * Fired when a reaction gets removed from a message.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionRemoveEvent} instead.
 */
@Deprecated
public class ReactionRemoveEvent extends sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionRemoveEvent {
	
	public ReactionRemoveEvent(IMessage message, IReaction reaction, IUser user) {
		super(message, reaction, user);
	}
}
