/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.gateway.retry;

import discord4j.common.json.payload.dispatch.Dispatch;

import javax.annotation.Nullable;
import java.time.Duration;

public class GatewayStateChanged implements Dispatch {

	public enum State {
		CONNECTED, DISCONNECTED, RETRY_STARTED, RETRY_SUCCEEDED, RETRY_FAILED
	}

	public static GatewayStateChanged connected() {
		return new GatewayStateChanged(State.CONNECTED, 0, null);
	}

	public static GatewayStateChanged disconnected() {
		return new GatewayStateChanged(State.DISCONNECTED, 0, null);
	}

	public static GatewayStateChanged retryStarted(Duration nextAttemptBackoff) {
		return new GatewayStateChanged(State.RETRY_STARTED, 1, nextAttemptBackoff);
	}

	public static GatewayStateChanged retrySucceeded(int currentAttempt) {
		return new GatewayStateChanged(State.RETRY_SUCCEEDED, currentAttempt, null);
	}

	public static GatewayStateChanged retryFailed(int currentAttempt, Duration nextAttemptBackoff) {
		return new GatewayStateChanged(State.RETRY_FAILED, currentAttempt, nextAttemptBackoff);
	}

	private final State state;
	private final int currentAttempt;
	private final Duration backoff;

	private GatewayStateChanged(State state, int currentAttempt, Duration backoff) {
		this.state = state;
		this.currentAttempt = currentAttempt;
		this.backoff = backoff;
	}

	public State getState() {
		return state;
	}

	public int getCurrentAttempt() {
		return currentAttempt;
	}

	@Nullable
	public Duration getBackoff() {
		return backoff;
	}

	@Override
	public String toString() {
		return "GatewayStateChanged[" +
				"state=" + state +
				']';
	}
}
