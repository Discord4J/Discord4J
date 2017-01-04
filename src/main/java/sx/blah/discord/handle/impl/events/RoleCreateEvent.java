package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IRole;

/**
 * This event is dispatched whenever a role is created.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.role.RoleCreateEvent} instead.
 */
@Deprecated
public class RoleCreateEvent extends sx.blah.discord.handle.impl.events.guild.role.RoleCreateEvent {
	
	public RoleCreateEvent(IRole role) {
		super(role);
	}
}
