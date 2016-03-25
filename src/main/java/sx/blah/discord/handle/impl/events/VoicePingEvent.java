package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.Event;

/**
 * This event is dispatched when a voice heartbeat is received.
 */
public class VoicePingEvent extends Event {
	/**
	 * The ping for the voice channel the user is currently in.
	 */
	private final long ping;

	public VoicePingEvent(long ping) {
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
