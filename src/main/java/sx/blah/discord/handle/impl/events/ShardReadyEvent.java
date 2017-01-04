package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IShard;

/**
 * This event is dispatched when a shard is ready to interact with the api.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.shard.ShardReadyEvent} instead.
 */
@Deprecated
public class ShardReadyEvent extends sx.blah.discord.handle.impl.events.shard.ShardReadyEvent {
	
	public ShardReadyEvent(IShard shard) {
		super(shard);
	}
}
