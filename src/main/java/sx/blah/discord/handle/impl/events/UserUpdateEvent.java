package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IUser;

/**
 * This is dispatched whenever a user updates his/her info
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.user.UserUpdateEvent} instead.
 */
@Deprecated
public class UserUpdateEvent extends sx.blah.discord.handle.impl.events.user.UserUpdateEvent {
	
	public UserUpdateEvent(IUser oldUser, IUser newUser) {
		super(oldUser, newUser);
	}
}
