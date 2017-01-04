package sx.blah.discord.handle.impl.events.guild.member;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

/**
 * This event is dispatched when a guild updates a user's roles.
 */
public class UserRoleUpdateEvent extends GuildMemberEvent {

	private final List<IRole> oldRoles, newRoles;
	
	public UserRoleUpdateEvent(IGuild guild, IUser user, List<IRole> oldRoles, List<IRole> newRoles) {
		super(guild, user);
		this.oldRoles = oldRoles;
		this.newRoles = newRoles;
	}

	/**
	 * Gets the old roles for the user.
	 *
	 * @return The old roles.
	 */
	public List<IRole> getOldRoles() {
		return oldRoles;
	}

	/**
	 * Gets the new roles for the user.
	 *
	 * @return The new roles.
	 */
	public List<IRole> getNewRoles() {
		return newRoles;
	}
}
