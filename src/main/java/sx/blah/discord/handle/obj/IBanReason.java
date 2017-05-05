package sx.blah.discord.handle.obj;

/**
 * Represents a banned user from a guild, with a nullable reason.
 */
public interface IBanReason {

	/**
	 * Returns the reason for this ban. May be null to signify no reason provided.
	 * @return The reason, or null if there isn't one
	 */
	String getReason();

	/**
	 * Returns the user banned.
	 * @return The banned user
	 */
	IUser getUser();

	/**
	 * Returns the guild the user was banned from.
	 * @return The guild the user was banned from
	 */
	IGuild getGuild();

}
