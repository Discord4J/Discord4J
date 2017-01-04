package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

/**
 * This event is dispatched when a guild updates a user's roles.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.member.UserRoleUpdateEvent} instead.
 */
@Deprecated
public class UserRoleUpdateEvent extends sx.blah.discord.handle.impl.events.guild.member.UserRoleUpdateEvent {
	
	public UserRoleUpdateEvent(IGuild guild, IUser user, List<IRole> oldRoles, List<IRole> newRoles) {
		super(guild, user, oldRoles, newRoles);
	}
}
