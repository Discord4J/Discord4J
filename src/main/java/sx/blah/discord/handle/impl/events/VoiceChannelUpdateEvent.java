package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a voice channel is updated.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelUpdateEvent} instead.
 */
@Deprecated
public class VoiceChannelUpdateEvent extends sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelUpdateEvent {
	
	public VoiceChannelUpdateEvent(IVoiceChannel oldVoiceChannel, IVoiceChannel newVoiceChannel) {
		super(oldVoiceChannel, newVoiceChannel);
	}
}
