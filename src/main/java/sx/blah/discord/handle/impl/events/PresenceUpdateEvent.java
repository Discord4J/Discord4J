package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Presences;

/**
 * This event is dispatched when a user changes his/her presence.
 */
public class PresenceUpdateEvent extends Event {

	private final IUser user;
	private final Presences oldPresence, newPresence;
	private final int index;

	public PresenceUpdateEvent(IUser user, Presences oldPresence, Presences newPresence, int index) {
		this.user = user;
		this.oldPresence = oldPresence;
		this.newPresence = newPresence;
		this.index = index;
	}

	/**
	 * Gets the user's new presence.
	 *
	 * @return The presence.
	 */
	public Presences getNewPresence() {
		return newPresence;
	}

	/**
	 * Gets the user's old presence.
	 *
	 * @return The presence.
	 */
	public Presences getOldPresence() {
		return oldPresence;
	}

	/**
	 * Gets the user involved.
	 *
	 * @return The user.
	 */
	public IUser getUser() {
		return user;
	}

	/**
	 * In the case of the client, the shard. Otherwise, the index.
	 *
	 * @return The index.
	 */
	public int getIndex() {
		return index;
	}
}
