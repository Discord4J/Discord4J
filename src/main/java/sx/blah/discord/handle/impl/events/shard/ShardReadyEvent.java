package sx.blah.discord.handle.impl.events.shard;

import sx.blah.discord.api.IShard;

/**
 * This event is dispatched when a shard is ready to interact with the api.
 */
public class ShardReadyEvent extends ShardEvent {
	
	public ShardReadyEvent(IShard shard) {
		super(shard);
	}
}
