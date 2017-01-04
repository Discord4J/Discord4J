package sx.blah.discord.handle.impl.events.guild.member;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;

/**
 * This is dispatched when a user is added/joins a guild.
 */
public class UserJoinEvent extends GuildMemberEvent {

	private final LocalDateTime joinTime;

	public UserJoinEvent(IGuild guild, IUser user, LocalDateTime when) {
		super(guild, user);
		this.joinTime = when;
	}

	/**
	 * Gets the timestamp for when the user joined the guild.
	 *
	 * @return The timestamp.
	 */
	public LocalDateTime getJoinTime() {
		return joinTime;
	}
}
