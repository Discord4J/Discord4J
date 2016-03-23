package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a user disconnects from a voice channel.
 */
public class UserVoiceChannelLeaveEvent extends Event {

	/**
	 * The channel the user left.
	 */
	private final IVoiceChannel oldChannel;

	public UserVoiceChannelLeaveEvent(IVoiceChannel oldChannel) {
		this.oldChannel = oldChannel;
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
