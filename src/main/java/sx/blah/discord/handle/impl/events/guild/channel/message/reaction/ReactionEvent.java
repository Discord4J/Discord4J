package sx.blah.discord.handle.impl.events.guild.channel.message.reaction;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;

/**
 * This represents a generic reaction event.
 */
public abstract class ReactionEvent extends MessageEvent {
	
	private final IReaction reaction;
	private final IUser user;
	
	public ReactionEvent(IMessage message, IReaction reaction, IUser user) {
		super(message);
		this.reaction = reaction;
		this.user = user;
	}
	
	/**
	 * Gets the reaction object.
	 *
	 * @return The reaction object.
	 */
	public IReaction getReaction() {
		return reaction;
	}
	
	/**
	 * Gets the user that did this action.
	 *
	 * @return The acting user
	 */
	public IUser getUser() {
		return user;
	}
	
	/**
	 * Gets the user count for this reaction.
	 *
	 * @return The user count
	 */
	public int getCount() {
		return reaction.getCount();
	}
}
