package sx.blah.discord.handle.impl.events.guild.member;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched when a user is pardoned from a ban.
 */
public class UserPardonEvent extends GuildMemberEvent {
	
	public UserPardonEvent(IGuild guild, IUser user) {
		super(guild, user);
	}
}
