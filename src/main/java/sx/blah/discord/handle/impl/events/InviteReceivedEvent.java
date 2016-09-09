package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IInvite;
import sx.blah.discord.handle.obj.IMessage;

/**
 * This event is dispatched when a message the bot receives includes an invite link.
 */
public class InviteReceivedEvent extends Event {

	private final IInvite[] invites;
	private final IMessage message;

	public InviteReceivedEvent(IInvite[] invites, IMessage message) {
		this.invites = invites;
		this.message = message;
	}

	/**
	 * Gets the invites received.
	 *
	 * @return The invites received.
	 */
	public IInvite[] getInvites() {
		return invites;
	}

	/**
	 * Gets the message which contains the invite.
	 *
	 * @return The message.
	 */
	public IMessage getMessage() {
		return message;
	}
}
