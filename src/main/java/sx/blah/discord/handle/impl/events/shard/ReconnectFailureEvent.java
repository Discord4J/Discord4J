/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.handle.impl.events.shard;

import sx.blah.discord.api.IShard;

/**
 * Dispatched when a reconnect attempt for a shard fails.
 *
 * <p>This is dispatched for every failed attempt. Use {@link #isShardAbandoned()} to determine if this was the final
 * attempt.
 */
public class ReconnectFailureEvent extends ShardEvent {

	protected final int curAttempt;
	protected final int maxAttempts;

	public ReconnectFailureEvent(IShard shard, int curAttempt, int maxAttempts) {
		super(shard);
		this.curAttempt = curAttempt;
		this.maxAttempts = maxAttempts;
	}

	/**
	 * Gets the attempt the {@link sx.blah.discord.api.internal.ReconnectManager} failed on.
	 *
	 * @return The current attempt.
	 */
	public int getCurrentAttempt() {
		return curAttempt;
	}

	/**
	 * Gets whether the shard will be abandoned (no further reconnects will be attempted).
	 *
	 * @return Whether the shard will be abandoned.
	 */
	public boolean isShardAbandoned() {
		return curAttempt == maxAttempts - 1;
	}
}
