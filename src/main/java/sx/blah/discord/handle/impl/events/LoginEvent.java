package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IShard;
import sx.blah.discord.api.events.Event;

/**
 * This event is fired when a shard has established an initial connection to the Discord gateway.
 * At this point, the bot has <b>not</b> received all of the necessary information to interact with all aspects of the api.
 * Wait for {@link ReadyEvent} to do so.
 */
public class LoginEvent extends Event {
	protected final IShard shard;

	public LoginEvent(IShard shard) {
		this.shard = shard;
	}

	/**
	 * Gets the shard that this event was fired for.
	 * @return The shard that has logged in.
	 */
	public IShard getShard() {
		return this.shard;
	}
}
