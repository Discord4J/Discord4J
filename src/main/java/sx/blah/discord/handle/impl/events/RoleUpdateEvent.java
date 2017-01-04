package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IRole;

/**
 * This event is dispatched whenever a guild role is modified.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.role.RoleUpdateEvent} instead.
 */
@Deprecated
public class RoleUpdateEvent extends sx.blah.discord.handle.impl.events.guild.role.RoleUpdateEvent {
	
	public RoleUpdateEvent(IRole oldRole, IRole newRole) {
		super(oldRole, newRole);
	}
}
