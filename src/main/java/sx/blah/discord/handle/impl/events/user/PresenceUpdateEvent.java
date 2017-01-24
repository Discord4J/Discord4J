package sx.blah.discord.handle.impl.events.user;

import sx.blah.discord.handle.obj.IPresence;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched when a user changes his/her presence.
 */
public class PresenceUpdateEvent extends UserEvent {

	private final IPresence oldPresence, newPresence;

	public PresenceUpdateEvent(IUser user, IPresence oldPresence, IPresence newPresence) {
		super(user);
		this.oldPresence = oldPresence;
		this.newPresence = newPresence;
	}

	/**
	 * Gets the user's new presence.
	 *
	 * @return The presence.
	 */
	public IPresence getNewPresence() {
		return newPresence;
	}

	/**
	 * Gets the user's old presence.
	 *
	 * @return The presence.
	 */
	public IPresence getOldPresence() {
		return oldPresence;
	}
}
