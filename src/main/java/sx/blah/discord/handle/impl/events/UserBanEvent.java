package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched when a user is banned from a guild.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.member.UserBanEvent} instead.
 */
@Deprecated
public class UserBanEvent extends sx.blah.discord.handle.impl.events.guild.member.UserBanEvent {
	
	public UserBanEvent(IGuild guild, IUser user) {
		super(guild, user);
	}
}
