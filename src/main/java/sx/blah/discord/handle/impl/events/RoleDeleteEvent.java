package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IRole;

/**
 * This event is dispatched after a role has been deleted from a guild.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.role.RoleDeleteEvent} instead.
 */
@Deprecated
public class RoleDeleteEvent extends sx.blah.discord.handle.impl.events.guild.role.RoleDeleteEvent {
	
	public RoleDeleteEvent(IRole role) {
		super(role);
	}
}
