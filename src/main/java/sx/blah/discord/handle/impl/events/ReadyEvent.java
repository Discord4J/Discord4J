package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IShard;
import sx.blah.discord.api.events.Event;

/**
 * This event is dispatched when a shard is ready to interact with the api.
 */
public class ReadyEvent extends Event {
	protected IShard shard;

	public ReadyEvent(IShard shard) {
		this.shard = shard;
	}

	/**
	 * Gets the shard that this event was fired for.
	 * @return The shard that has become ready.
	 */
	public IShard getShard() {
		return this.shard;
	}
}
