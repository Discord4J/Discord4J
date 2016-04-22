package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.Event;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a user disconnects from a voice channel.
 */
public class UserVoiceChannelLeaveEvent extends Event {

	/**
	 * The user that has left.
	 */
	private final IUser user;
	
	/**
	 * The channel the user left.
	 */
	private final IVoiceChannel oldChannel;

	public UserVoiceChannelLeaveEvent(IUser user, IVoiceChannel oldChannel) {
		this.user = user;
		this.oldChannel = oldChannel;
	}

	/**
	 * Retrieves the user that has left the channel.
	 *
	 * @return The user that has left the channel.
	 */
	public IUser getUser() {
		return user;
	}
	
	/**
	 * Gets the voice channel this user left.
	 *
	 * @return The voice channel.
	 */
	public IVoiceChannel getChannel() {
		return oldChannel;
	}
}
