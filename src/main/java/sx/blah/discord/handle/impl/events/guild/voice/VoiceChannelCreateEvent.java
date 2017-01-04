package sx.blah.discord.handle.impl.events.guild.voice;

import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a voice channel is created.
 */
public class VoiceChannelCreateEvent extends VoiceChannelEvent {
	
	public VoiceChannelCreateEvent(IVoiceChannel voiceChannel) {
		super(voiceChannel);
	}
}
