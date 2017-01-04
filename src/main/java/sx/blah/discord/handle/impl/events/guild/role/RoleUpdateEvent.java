package sx.blah.discord.handle.impl.events.guild.role;

import sx.blah.discord.handle.obj.IRole;

/**
 * This event is dispatched whenever a guild role is modified.
 */
public class RoleUpdateEvent extends RoleEvent {

	private final IRole oldRole, newRole;

	public RoleUpdateEvent(IRole oldRole, IRole newRole) {
		super(newRole);
		this.oldRole = oldRole;
		this.newRole = newRole;
	}

	/**
	 * Gets the original version of the role.
	 *
	 * @return The old role.
	 */
	public IRole getOldRole() {
		return oldRole;
	}

	/**
	 * Gets the new version of the role.
	 *
	 * @return The new role.
	 */
	public IRole getNewRole() {
		return newRole;
	}
}
