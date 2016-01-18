package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched when a user is pardoned from a ban.
 */
public class UserPardonEvent extends Event {
	
	private final IUser user;
	private final IGuild guild;
	
	public UserPardonEvent(IUser user, IGuild guild) {
		this.user = user;
		this.guild = guild;
	}
	
	/**
	 * Gets the user that was pardoned.
	 *
	 * @return The pardoned user.
	 */
	public IUser getUser() {
		return user;
	}
	
	/**
	 * Gets the guild the user was pardoned from.
	 *
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return guild;
	}
}
