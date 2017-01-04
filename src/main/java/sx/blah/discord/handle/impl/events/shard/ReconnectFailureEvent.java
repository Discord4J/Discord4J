package sx.blah.discord.handle.impl.events.shard;

import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.ReconnectManager;

/**
 * Fired when a reconnect attempt for a shard fails.
 * Note: This does not necessarily mean that the shard will be abandoned. This is fired for every failed <b>attempt</b>.
 * Use {@link #getCurrentAttempt()} ()} to determine if the shard will be abandoned.
 */
public class ReconnectFailureEvent extends ShardEvent {

	protected final int curAttempt;

	public ReconnectFailureEvent(IShard shard, int curAttempt) {
		super(shard);
		this.curAttempt = curAttempt;
	}

	/**
	 * Gets the attempt the {@link ReconnectManager} failed on.
	 * @return The current attempt.
	 */
	public int getCurrentAttempt() {
		return this.curAttempt;
	}
}
