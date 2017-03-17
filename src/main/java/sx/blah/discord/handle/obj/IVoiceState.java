package sx.blah.discord.handle.obj;

/**
 * Represents a {@link sx.blah.discord.handle.impl.obj.User}'s voice state in a guild.
 */
public interface IVoiceState {

	/**
	 * Gets the guild for this voice state.
	 * @return The guild.
	 */
	IGuild getGuild();

	/**
	 * Gets the voice channel for this voice state.
	 * @return The voice channel.
	 */
	IVoiceChannel getChannel();

	/**
	 * Gets the user for this voice state.
	 * @return The user.
	 */
	IUser getUser();

	/**
	 * Gets the session id for this voice state.
	 * Note: Probably not useful to you.
	 * @return The session id.
	 */
	String getSessionID();

	/**
	 * Whether the user represented by this voice state is deafened on the guild-level.
	 * @return Guild-level deaf state.
	 */
	boolean isDeafened();

	/**
	 * Whether the user represented by this voice state is muted on the guild-level.
	 * @return Guild-level mute state.
	 */
	boolean isMuted();

	/**
	 * Whether the user represented by this voice state has deafened themselves in their client.
	 * @return Client-level deaf state.
	 */
	boolean isSelfDeafened();

	/**
	 * Whether the user represented by this voice state has muted themselves in their client.
	 * @return Client-level mute state.
	 */
	boolean isSelfMuted();

	/**
	 * Whether the user represented by this voice state is muted by the bot user.
	 * @return User suppressed state.
	 */
	boolean isSuppressed();
}
