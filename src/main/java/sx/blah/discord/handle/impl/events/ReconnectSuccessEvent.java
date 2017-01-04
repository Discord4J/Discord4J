package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IShard;

/**
 * Fired when a shard has succeeded in connecting to the gateway.
 * Either {@link LoginEvent} or {@link ResumedEvent} should follow shortly.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.shard.ReconnectSuccessEvent} instead.
 */
@Deprecated
public class ReconnectSuccessEvent extends sx.blah.discord.handle.impl.events.shard.ReconnectSuccessEvent {
	
	public ReconnectSuccessEvent(IShard shard) {
		super(shard);
	}
}
