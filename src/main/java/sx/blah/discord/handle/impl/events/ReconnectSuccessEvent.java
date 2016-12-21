package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IShard;
import sx.blah.discord.api.events.Event;

/**
 * Fired when a shard has succeeded in connecting to the gateway.
 * Either {@link LoginEvent} or {@link ResumedEvent} should follow shortly.
 */
public class ReconnectSuccessEvent extends Event {

	protected final IShard shard;

	public ReconnectSuccessEvent(IShard shard) {
		this.shard = shard;
	}

	/**
	 * Gets the shard that this event was fired for.
	 * @return The shard that has logged in.
	 */
	public IShard getShard() {
		return this.shard;
	}
}
