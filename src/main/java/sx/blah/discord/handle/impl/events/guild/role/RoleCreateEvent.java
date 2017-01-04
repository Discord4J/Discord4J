package sx.blah.discord.handle.impl.events.guild.role;

import sx.blah.discord.handle.obj.IRole;

/**
 * This event is dispatched whenever a role is created.
 */
public class RoleCreateEvent extends RoleEvent {
	
	public RoleCreateEvent(IRole role) {
		super(role);
	}
}
