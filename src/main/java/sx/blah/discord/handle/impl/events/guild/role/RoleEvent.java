package sx.blah.discord.handle.impl.events.guild.role;

import sx.blah.discord.handle.impl.events.guild.GuildEvent;
import sx.blah.discord.handle.obj.IRole;

/**
 * This represents a generic role event.
 */
public abstract class RoleEvent extends GuildEvent {
	
	private final IRole role;
	
	public RoleEvent(IRole role) {
		super(role.getGuild());
		this.role = role;
	}
	
	/**
	 * This gets the role involved in this event.
	 *
	 * @return The role.
	 */
	public IRole getRole() {
		return role;
	}
}
