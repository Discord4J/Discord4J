package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a voice heartbeat is received.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.voice.VoicePingEvent} instead.
 */
@Deprecated
public class VoicePingEvent extends sx.blah.discord.handle.impl.events.guild.voice.VoicePingEvent {
	
	public VoicePingEvent(IVoiceChannel channel, long ping) {
		super(channel, ping);
	}
}
