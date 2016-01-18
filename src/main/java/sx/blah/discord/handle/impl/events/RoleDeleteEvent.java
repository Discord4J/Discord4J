package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;

/**
 * This event is dispatched after a role has been deleted from a guild.
 */
public class RoleDeleteEvent extends Event {
	
	private final IRole role;
	private final IGuild guild;
	
	public RoleDeleteEvent(IRole role, IGuild guild) {
		this.role = role;
		this.guild = guild;
	}
	
	/**
	 * Gets the role that was deleted.
	 * 
	 * @return The deleted role.
	 */
	public IRole getRole() {
		return role;
	}
	
	/**
	 * Gets the guild the role was from.
	 * 
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return guild;
	}
}
