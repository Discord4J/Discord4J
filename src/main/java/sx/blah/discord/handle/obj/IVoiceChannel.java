package sx.blah.discord.handle.obj;

import sx.blah.discord.handle.AudioChannel;
import sx.blah.discord.util.DiscordException;

import java.util.List;

/**
 * Represents a voice channel.
 */
public interface IVoiceChannel extends IChannel {
	/**
	 * Makes the bot user join this voice channel.
	 */
	void join();

	/**
	 * Makes the bot user leave this voice channel.
	 */
	void leave();

	/**
	 * Gets the audio channel of this guild. This throws an exception if the bot isn't in this channel yet.
	 *
	 * @return The audio channel.
	 *
	 * @throws DiscordException
	 */
	AudioChannel getAudioChannel() throws DiscordException;

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
