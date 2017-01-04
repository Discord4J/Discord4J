package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched when a guild member is removed/leaves from a guild
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent} instead.
 */
@Deprecated
public class UserLeaveEvent extends sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent {
	
	public UserLeaveEvent(IGuild guild, IUser user) {
		super(guild, user);
	}
}
