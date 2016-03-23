package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a user connects to a voice channel.
 */
public class UserVoiceChannelJoinEvent extends Event {

	/**
	 * The channel the user joined.
	 */
	private final IVoiceChannel newChannel;

	public UserVoiceChannelJoinEvent(IVoiceChannel newChannel) {
		this.newChannel = newChannel;
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
