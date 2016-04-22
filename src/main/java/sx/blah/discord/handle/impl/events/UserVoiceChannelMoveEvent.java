package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.Event;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a user moves from one voice channel to another.
 */
public class UserVoiceChannelMoveEvent extends Event {

	/**
	 * The user that has moved.
	 */
	private final IUser user;
	
	/**
	 * The channel the user left.
	 */
	private final IVoiceChannel oldChannel;
	
	/**
	 * The channel the user joined.
	 */
	private final IVoiceChannel newChannel;

	public UserVoiceChannelMoveEvent(IUser user, IVoiceChannel oldChannel, IVoiceChannel newChannel) {
		this.user = user;
		this.oldChannel = oldChannel;
		this.newChannel = newChannel;
	}
	
	/**
	 * Retrieves the user that has moved to another channel.
	 *
	 * @return The user that has moved.
	 */
	public IUser getUser() {
		return user;
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
