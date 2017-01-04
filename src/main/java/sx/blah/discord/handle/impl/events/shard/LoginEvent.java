package sx.blah.discord.handle.impl.events.shard;

import sx.blah.discord.api.IShard;
import sx.blah.discord.handle.impl.events.ReadyEvent;

/**
 * This event is fired when a shard has established an initial connection to the Discord gateway.
 * At this point, the bot has <b>not</b> received all of the necessary information to interact with all aspects of the api.
 * Wait for {@link ReadyEvent} to do so.
 */
public class LoginEvent extends ShardEvent {
	
	public LoginEvent(IShard shard) {
		super(shard);
	}
}
