package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;

/**
 * Fired when a reaction gets removed from a message.
 */
public class ReactionRemoveEvent extends Event {

	private final IMessage message;
	private final IReaction reaction;
	private final IUser user;

	public ReactionRemoveEvent(IMessage message, IReaction reaction, IUser user) {
		this.message = message;
		this.reaction = reaction;
		this.user = user;
	}

	/**
	 * Gets the message this reaction is on.
	 * @return The reaction's message
	 */
	public IMessage getMessage() {
		return message;
	}

	/**
	 * Gets the reaction object.
	 * @return The reaction object.
	 */
	public IReaction getReaction() {
		return reaction;
	}

	/**
	 * Gets the user that did this action.
	 * @return The acting user
	 */
	public IUser getUser() {
		return user;
	}

	/**
	 * Gets the user count for this reaction.
	 * @return The user count
	 */
	public int getCount() {
		return reaction.getCount();
	}

}
