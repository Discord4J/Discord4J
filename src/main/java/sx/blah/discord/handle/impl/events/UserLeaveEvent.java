package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched when a guild member is removed/leaves from a guild
 */
public class UserLeaveEvent extends Event {
	private final IGuild guild;
	private final IUser user;

	public UserLeaveEvent(IGuild guild, IUser user) {
		this.guild = guild;
		this.user = user;
	}

	/**
	 * The user involved.
	 *
	 * @return The user.
	 */
	public IUser getUser() {
		return user;
	}

	/**
	 * The guild involved.
	 *
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return guild;
	}
}
