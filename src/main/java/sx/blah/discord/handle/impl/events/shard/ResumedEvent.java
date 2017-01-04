package sx.blah.discord.handle.impl.events.shard;

import sx.blah.discord.api.IShard;

/**
 * Fired when the client receives a RESUMED payload from the gateway.
 * Missed events should replay after this.
 */
public class ResumedEvent extends ShardEvent {
	
	public ResumedEvent(IShard shard) {
		super(shard);
	}
}
