package sx.blah.discord.handle.impl.events.guild.voice;

import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a voice channel is deleted.
 */
public class VoiceChannelDeleteEvent extends VoiceChannelEvent {
	
	public VoiceChannelDeleteEvent(IVoiceChannel voiceChannel) {
		super(voiceChannel);
	}
}
