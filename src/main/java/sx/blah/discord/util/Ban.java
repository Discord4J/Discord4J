package sx.blah.discord.util;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * Represents a banned user from a guild, with a nullable reason.
 */
public class Ban {

	/**
	 * The maximum reason length. Also applies to kicks.
	 */
	public static final int MAX_REASON_LENGTH = 512;

	private final IGuild guild;
	private final IUser user;
	private final String reason;

	public Ban(IGuild guild, IUser user, String reason) {
		this.guild = guild;
		this.user = user;
		this.reason = reason;
	}

	/**
	 * Returns the reason for this ban. May be null to signify no reason provided.
	 *
	 * @return The reason, or null if there isn't one
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * Returns the user banned.
	 *
	 * @return The banned user
	 */
	public IUser getUser() {
		return user;
	}

	/**
	 * Returns the guild the user was banned from.
	 *
	 * @return The guild the user was banned from
	 */
	public IGuild getGuild() {
		return guild;
	}
}
