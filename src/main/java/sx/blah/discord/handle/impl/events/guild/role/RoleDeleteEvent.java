package sx.blah.discord.handle.impl.events.guild.role;

import sx.blah.discord.handle.obj.IRole;

/**
 * This event is dispatched after a role has been deleted from a guild.
 */
public class RoleDeleteEvent extends RoleEvent {
	
	public RoleDeleteEvent(IRole role) {
		super(role);
	}
}
