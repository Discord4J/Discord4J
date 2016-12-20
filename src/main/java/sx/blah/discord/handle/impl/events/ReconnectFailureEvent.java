package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IShard;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.internal.ReconnectManager;

/**
 * Fired when a reconnect attempt for a shard fails.
 * Note: This does not necessarily mean that the shard will be abandoned. This is fired for every failed <b>attempt</b>.
 * Use {@link #getCurAttempt()} ()} to determine if the shard will be abandoned.
 */
public class ReconnectFailureEvent extends Event {

	protected final IShard shard;
	protected final int curAttempt;

	public ReconnectFailureEvent(IShard shard, int curAttempt) {
		this.shard = shard;
		this.curAttempt = curAttempt;
	}

	/**
	 * Gets the shard that this event was fired for.
	 * @return The shard that has logged in.
	 */
	public IShard getShard() {
		return this.shard;
	}

	/**
	 * Gets the attempt the {@link ReconnectManager} failed on.
	 * @return The current attempt.
	 */
	public int getCurAttempt() {
		return this.curAttempt;
	}
}
