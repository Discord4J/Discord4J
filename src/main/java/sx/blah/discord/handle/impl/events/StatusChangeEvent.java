package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.impl.events.user.UserEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Status;

/**
 * This event was dispatched when a user's status changed.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent} instead.
 */
@Deprecated
public class StatusChangeEvent extends UserEvent {

	private final Status oldStatus, newStatus;

	public StatusChangeEvent(IUser user, Status oldStatus, Status newStatus) {
		super(user);
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
	}

	/**
	 * Gets the new status.
	 *
	 * @return The new status.
	 */
	public Status getNewStatus() {
		return newStatus;
	}

	/**
	 * Gets the old status.
	 *
	 * @return The old status.
	 */
	public Status getOldStatus() {
		return oldStatus;
	}

}
