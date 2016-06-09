package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Status;

/**
 * This event is dispatched when a user's status changes.
 */
public class StatusChangeEvent extends Event {

	private final IGuild guild;
	private final IUser user;
	private final Status oldStatus, newStatus;

	public StatusChangeEvent(IGuild guild, IUser user, Status oldStatus, Status newStatus) {
		this.guild = guild;
		this.user = user;
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
	 * Gets the user involved.
	 *
	 * @return The user.
	 */
	public IUser getUser() {
		return user;
	}

	/**
	 * Gets the old status.
	 *
	 * @return The old status.
	 */
	public Status getOldStatus() {
		return oldStatus;
	}

	/**
	 * Gets the guild involved.
	 *
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return guild;
	}
}
