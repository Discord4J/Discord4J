package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Status;

/**
 * This event is dispatched when a user's status changes.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.user.StatusChangeEvent} instead.
 */
@Deprecated
public class StatusChangeEvent extends sx.blah.discord.handle.impl.events.user.StatusChangeEvent {
	
	public StatusChangeEvent(IUser user, Status oldStatus, Status newStatus) {
		super(user, oldStatus, newStatus);
	}
}
