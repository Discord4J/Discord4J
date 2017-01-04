package sx.blah.discord.handle.impl.events.user;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IUser;

/**
 * This represents a generic user event.
 */
public abstract class UserEvent extends Event {
	
	private final IUser user;
	
	public UserEvent(IUser user) {
		this.user = user;
	}
	
	/**
	 * This gets the user involved in this event.
	 *
	 * @return The user.
	 */
	public IUser getUser() {
		return user;
	}
}
