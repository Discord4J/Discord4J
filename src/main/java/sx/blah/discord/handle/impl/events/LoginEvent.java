package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IShard;

/**
 * This event is fired when a shard has established an initial connection to the Discord gateway.
 * At this point, the bot has <b>not</b> received all of the necessary information to interact with all aspects of the api.
 * Wait for {@link ReadyEvent} to do so.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.shard.LoginEvent} instead.
 */
@Deprecated
public class LoginEvent extends sx.blah.discord.handle.impl.events.shard.LoginEvent {
	
	public LoginEvent(IShard shard) {
		super(shard);
	}
}
