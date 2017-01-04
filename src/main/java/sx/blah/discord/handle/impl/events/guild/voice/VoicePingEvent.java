package sx.blah.discord.handle.impl.events.guild.voice;

import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a voice heartbeat is received.
 */
public class VoicePingEvent extends VoiceChannelEvent {
	/**
	 * The ping for the voice channel the user is currently in.
	 */
	private final long ping;

	public VoicePingEvent(IVoiceChannel channel, long ping) {
		super(channel);
		this.ping = ping;
	}

	/**
	 * Gets the ping for the voice channel the user is currently in.
	 *
	 * @return The ping.
	 */
	public long getPing() {
		return ping;
	}
}
