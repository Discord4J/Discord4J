package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.Event;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a user connects to a voice channel.
 */
public class UserVoiceChannelJoinEvent extends Event {
	
	/**
	 * The user that has joined.
	 */
	private final IUser user;
	
	/**
	 * The channel the user joined.
	 */
	private final IVoiceChannel newChannel;

	public UserVoiceChannelJoinEvent(IUser user, IVoiceChannel newChannel) {
		this.user = user;
		this.newChannel = newChannel;
	}
	
	/**
	 * Retrieves the user that has joined the channel.
	 *
	 * @return The user that has joined the channel.
	 */
	public IUser getUser() {
		return user;
	}
	
	/**
	 * Gets the voice channel this user joined.
	 *
	 * @return The voice channel.
	 */
	public IVoiceChannel getChannel() {
		return newChannel;
	}
}
