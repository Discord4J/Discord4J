package sx.blah.discord.handle.impl.events.user;

import sx.blah.discord.handle.obj.IUser;

/**
 * This is dispatched whenever a user updates his/her info
 */
public class UserUpdateEvent extends UserEvent {

	private IUser oldUser, newUser;

	public UserUpdateEvent(IUser oldUser, IUser newUser) {
		super(newUser);
		this.oldUser = oldUser;
		this.newUser = newUser;
	}

	/**
	 * Gets the old user info
	 *
	 * @return The old user object
	 */
	public IUser getOldUser() {
		return oldUser;
	}

	/**
	 * Gets the new user info
	 *
	 * @return The new user object
	 */
	public IUser getNewUser() {
		return newUser;
	}
}
