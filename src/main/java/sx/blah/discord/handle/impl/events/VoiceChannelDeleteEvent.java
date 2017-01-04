package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a voice channel is deleted.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelDeleteEvent} instead.
 */
@Deprecated
public class VoiceChannelDeleteEvent extends sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelDeleteEvent {
	
	public VoiceChannelDeleteEvent(IVoiceChannel voiceChannel) {
		super(voiceChannel);
	}
}
