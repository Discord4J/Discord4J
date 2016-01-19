package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a voice channel is created.
 */
public class VoiceChannelCreateEvent extends Event {
	
	private final IVoiceChannel channel;
	
	public VoiceChannelCreateEvent(IVoiceChannel channel) {
		this.channel = channel;
	}
	
	/**
	 * Gets the channel involved.
	 *
	 * @return The channel.
	 */
	public IVoiceChannel getChannel() {
		return channel;
	}
}
