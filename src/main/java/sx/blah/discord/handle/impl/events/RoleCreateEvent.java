package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;

/**
 * This event is dispatched whenever a role is created.
 */
public class RoleCreateEvent extends Event {
	
	private final IRole role;
	private final IGuild guild;
	
	public RoleCreateEvent(IRole role, IGuild guild) {
		this.role = role;
		this.guild = guild;
	}
	
	/**
	 * Gets the newly created role.
	 * 
	 * @return The role.
	 */
	public IRole getRole() {
		return role;
	}
	
	/**
	 * Gets the guild the role was created for.
	 * 
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return guild;
	}
}
