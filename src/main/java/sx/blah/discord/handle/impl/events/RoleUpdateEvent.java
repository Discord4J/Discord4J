package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;

/**
 * This event is dispatched whenever a guild role is modified.
 */
public class RoleUpdateEvent extends Event {
	
	private final IRole oldRole, newRole;
	private final IGuild guild;
	
	public RoleUpdateEvent(IRole oldRole, IRole newRole, IGuild guild) {
		this.oldRole = oldRole;
		this.newRole = newRole;
		this.guild = guild;
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
	
	/**
	 * Gets the guild the role is for.
	 * 
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return guild;
	}
}
