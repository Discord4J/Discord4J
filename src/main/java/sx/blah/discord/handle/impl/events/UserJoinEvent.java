package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;

/**
 * This is dispatched when a user is added/joins a guild.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent} instead.
 */
@Deprecated
public class UserJoinEvent extends sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent {
	
	public UserJoinEvent(IGuild guild, IUser user, LocalDateTime when) {
		super(guild, user, when);
	}
}
