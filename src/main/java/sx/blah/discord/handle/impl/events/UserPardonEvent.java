package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched when a user is pardoned from a ban.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.member.UserPardonEvent} instead.
 */
@Deprecated
public class UserPardonEvent extends sx.blah.discord.handle.impl.events.guild.member.UserPardonEvent {
	
	public UserPardonEvent(IGuild guild, IUser user) {
		super(guild, user);
	}
}
