package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

/**
 * This event is dispatched when a guild updates a user's roles.
 */
public class UserRoleUpdateEvent extends Event {
	
	private final List<IRole> oldRoles, newRoles;
	private final IUser user;
	
	public UserRoleUpdateEvent(List<IRole> oldRoles, List<IRole> newRoles, IUser user) {
		this.oldRoles = oldRoles;
		this.newRoles = newRoles;
		this.user = user;
	}
	
	/**
	 * Gets the old roles for the user.
	 *
	 * @return The old roles.
	 */
	public List<IRole> getOldRoles() {
		return oldRoles;
	}
	
	/**
	 * Gets the new roles for the user.
	 *
	 * @return The new roles.
	 */
	public List<IRole> getNewRoles() {
		return newRoles;
	}
	
	/**
	 * Gets the user involved.
	 *
	 * @return The user.
	 */
	public IUser getUser() {
		return user;
	}
}
