package sx.blah.discord.handle.impl.events.guild.member;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * This event is dispatched when a guild member is removed/leaves from a guild
 */
public class UserLeaveEvent extends GuildMemberEvent {
	
	public UserLeaveEvent(IGuild guild, IUser user) {
		super(guild, user);
	}
}
