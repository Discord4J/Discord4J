package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IShard;

/**
 * Fired when the client receives a RESUMED payload from the gateway.
 * Missed events should replay after this.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.shard.ResumedEvent} instead.
 */
@Deprecated
public class ResumedEvent extends sx.blah.discord.handle.impl.events.shard.ResumedEvent {
	
	public ResumedEvent(IShard shard) {
		super(shard);
	}
}
