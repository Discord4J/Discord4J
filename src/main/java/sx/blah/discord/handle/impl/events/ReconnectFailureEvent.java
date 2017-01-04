package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.ReconnectManager;

/**
 * Fired when a reconnect attempt for a shard fails.
 * Note: This does not necessarily mean that the shard will be abandoned. This is fired for every failed <b>attempt</b>.
 * Use {@link #getCurAttempt()} ()} to determine if the shard will be abandoned.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.shard.ReconnectFailureEvent} instead.
 */
@Deprecated
public class ReconnectFailureEvent extends sx.blah.discord.handle.impl.events.shard.ReconnectFailureEvent {
	
	public ReconnectFailureEvent(IShard shard, int curAttempt) {
		super(shard, curAttempt);
	}
	
	/**
	 * Gets the attempt the {@link ReconnectManager} failed on.
	 * @return The current attempt.
	 */
	public int getCurAttempt() {
		return this.curAttempt;
	}
}
