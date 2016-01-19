package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched when a user is banned from a guild.
 */
public class UserBanEvent extends Event {
	
	private final IUser user;
	private final IGuild guild;
	
	public UserBanEvent(IUser user, IGuild guild) {
		this.user = user;
		this.guild = guild;
	}
	
	/**
	 * Gets the user that was banned.
	 * 
	 * @return The banned user.
	 */
	public IUser getUser() {
		return user;
	}
	
	/**
	 * Gets the guild the user was banned from.
	 * 
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return guild;
	}
}
