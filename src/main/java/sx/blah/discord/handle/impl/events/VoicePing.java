package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;

/**
 * @author lclc98
 */
public class VoicePing extends Event {
	private final long ping;

	public VoicePing(long ping) {this.ping = ping;}

	public long getPing() {
		return ping;
	}
}
