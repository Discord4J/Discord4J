package sx.blah.discord.util;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * A ban entry in a guild.
 */
public class Ban {

	/**
	 * The maximum length of a ban or kick reason.
	 */
	public static final int MAX_REASON_LENGTH = 512;

	/**
	 * The guild the ban is in.
	 */
	private final IGuild guild;
	/**
	 * The user who is banned.
	 */
	private final IUser user;
	/**
	 * The nullable reason for the ban.
	 */
	private final String reason;

	public Ban(IGuild guild, IUser user, String reason) {
		this.guild = guild;
		this.user = user;
		this.reason = reason;
	}

	/**
	 * Gets the nullable reason for the ban.
	 *
	 * @return The nullable reason for the ban.
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * Gets the user who is banned.
	 *
	 * @return The user who is banned.
	 */
	public IUser getUser() {
		return user;
	}

	/**
	 * Gets the guild the ban is in.
	 *
	 * @return The guild the ban is in.
	 */
	public IGuild getGuild() {
		return guild;
	}
}
