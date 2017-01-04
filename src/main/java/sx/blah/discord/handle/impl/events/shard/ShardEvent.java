package sx.blah.discord.handle.impl.events.shard;

import sx.blah.discord.api.IShard;
import sx.blah.discord.api.events.Event;

/**
 * This represents a generic shard event.
 */
public abstract class ShardEvent extends Event {
	
	private final IShard shard;
	
	public ShardEvent(IShard shard) {
		this.shard = shard;
	}
	
	/**
	 * This gets the shard object involved in this event.
	 *
	 * @return The shard.
	 */
	public IShard getShard() {
		return shard;
	}
}
