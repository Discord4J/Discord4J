package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a voice channel is created.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelCreateEvent} instead.
 */
@Deprecated
public class VoiceChannelCreateEvent extends sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelCreateEvent {
	
	public VoiceChannelCreateEvent(IVoiceChannel voiceChannel) {
		super(voiceChannel);
	}
}
