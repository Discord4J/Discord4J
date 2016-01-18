package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a voice channel is deleted.
 */
public class VoiceChannelDeleteEvent extends Event {
	
	private final IVoiceChannel channel;
	
	public VoiceChannelDeleteEvent(IVoiceChannel channel) {
		this.channel = channel;
	}
	
	/**
	 * Gets the channel involved.
	 *
	 * @return The channel.
	 */
	public IVoiceChannel getVoiceChannel() {
		return channel;
	}
}
