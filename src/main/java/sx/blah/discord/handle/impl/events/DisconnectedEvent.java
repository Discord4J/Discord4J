package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IShard;

/**
 * This event is dispatched when either a shard loses connection to discord or is logged out.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.shard.DisconnectedEvent} instead.
 */
@Deprecated
public class DisconnectedEvent extends sx.blah.discord.handle.impl.events.shard.DisconnectedEvent {
	
	public DisconnectedEvent(Reason reason, IShard shard) {
		super(reason, shard);
	}
}
