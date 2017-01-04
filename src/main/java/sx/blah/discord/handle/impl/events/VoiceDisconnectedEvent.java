package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when either the client loses connection to discord or is logged out.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.guild.voice.VoiceDisconnectedEvent} instead.
 */
@Deprecated
public class VoiceDisconnectedEvent extends sx.blah.discord.handle.impl.events.guild.voice.VoiceDisconnectedEvent {
	
	public VoiceDisconnectedEvent(IVoiceChannel channel, Reason reason) {
		super(channel, reason);
	}
	
	public VoiceDisconnectedEvent(IGuild guild, Reason reason) {
		super(guild, reason);
	}
}

