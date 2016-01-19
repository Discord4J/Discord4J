package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a voice channel is updated.
 */
public class VoiceChannelUpdateEvent extends Event {
	
	private final IVoiceChannel oldVoiceChannel, newVoiceChannel;
	
	public VoiceChannelUpdateEvent(IVoiceChannel oldVoiceChannel, IVoiceChannel newVoiceChannel) {
		this.oldVoiceChannel = oldVoiceChannel;
		this.newVoiceChannel = newVoiceChannel;
	}
	
	/**
	 * Gets the original voice channel.
	 *
	 * @return The un-updated instance of the voice channel.
	 */
	public IVoiceChannel getOldVoiceChannel() {
		return oldVoiceChannel;
	}
	
	/**
	 * Gets the new voice channel.
	 *
	 * @return The updated instance of the voice channel.
	 */
	public IVoiceChannel getNewVoiceChannel() {
		return newVoiceChannel;
	}
}
