package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a user moves from one voice channel to another.
 */
public class UserVoiceChannelMoveEvent extends Event {

	/**
	 * The channel the user left.
	 */
	private final IVoiceChannel oldChannel;
	/**
	 * The channel the user joined.
	 */
	private final IVoiceChannel newChannel;

	public UserVoiceChannelMoveEvent(IVoiceChannel oldChannel, IVoiceChannel newChannel) {
		this.oldChannel = oldChannel;
		this.newChannel = newChannel;
	}

	/**
	 * Gets the voice channel this user left.
	 *
	 * @return The voice channel.
	 */
	public IVoiceChannel getOldChannel() {
		return oldChannel;
	}

	/**
	 * Gets the voice channel this user joined.
	 *
	 * @return The voice channel.
	 */
	public IVoiceChannel getNewChannel() {
		return newChannel;
	}
}
