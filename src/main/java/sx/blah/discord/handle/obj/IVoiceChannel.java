package sx.blah.discord.handle.obj;

import sx.blah.discord.util.MissingPermissionsException;

import java.util.List;

/**
 * Represents a voice channel.
 */
public interface IVoiceChannel extends IChannel {
	/**
	 * This gets the maximum amount of users allowed in this voice channel.
	 *
	 * @return The maximum amount of users allowed (or 0 if there is not set limit)
	 */
	int getUserLimit();

	/**
	 * Makes the bot user join this voice channel.
	 *
	 * @throws MissingPermissionsException
	 */
	void join() throws MissingPermissionsException;

	/**
	 * Makes the bot user leave this voice channel.
	 */
	void leave();

	/**
	 * Checks if this voice channel is connected to by our user.
	 *
	 * @return True if connected, false if otherwise.
	 */
	boolean isConnected();

	/**
	 * {@inheritDoc}
	 */
	IVoiceChannel copy();

	/**
	 * This collects all users connected to this voice channel and returns them in a list.
	 *
	 * @return The connected users.
	 */
	List<IUser> getConnectedUsers();
}
