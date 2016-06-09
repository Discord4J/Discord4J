package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.time.LocalDateTime;

/**
 * This is dispatched when a user is added/joins a guild.
 */
public class UserJoinEvent extends Event {

	private final IGuild guild;
	private final LocalDateTime joinTime;
	private final IUser userJoined;

	public UserJoinEvent(IGuild guild, IUser user, LocalDateTime when) {
		this.guild = guild;
		this.joinTime = when;
		this.userJoined = user;
	}

	/**
	 * Gets the timestamp for when the user joined the guild.
	 *
	 * @return The timestamp.
	 */
	public LocalDateTime getJoinTime() {
		return joinTime;
	}

	/**
	 * Gets the user involved.
	 *
	 * @return The user.
	 */
	public IUser getUser() {
		return userJoined;
	}

	/**
	 * Gets the guild involved.
	 *
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return guild;
	}
}
