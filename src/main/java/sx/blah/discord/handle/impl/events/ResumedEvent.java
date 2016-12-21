package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IShard;
import sx.blah.discord.api.events.Event;

/**
 * Fired when the client receives a RESUMED payload from the gateway.
 * Missed events should replay after this.
 */
public class ResumedEvent extends Event {
	private final IShard shard;

	public ResumedEvent(IShard shard) {
		this.shard = shard;
	}

	public  IShard getShard() {
		return this.shard;
	}
}
