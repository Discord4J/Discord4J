package sx.blah.discord.handle.impl.events.shard;

import sx.blah.discord.api.IShard;

/**
 * Fired when a shard has succeeded in connecting to the gateway.
 * Either {@link LoginEvent} or {@link ResumedEvent} should follow shortly.
 */
public class ReconnectSuccessEvent extends ShardEvent {
	
	public ReconnectSuccessEvent(IShard shard) {
		super(shard);
	}
}
