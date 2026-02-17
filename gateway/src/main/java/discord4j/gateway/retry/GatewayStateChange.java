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

import discord4j.common.close.CloseStatus;
import discord4j.common.close.DisconnectBehavior;
import discord4j.discordjson.json.gateway.Dispatch;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

public class GatewayStateChange implements Dispatch {

    public enum State {
        CONNECTED, DISCONNECTED, DISCONNECTED_RESUME, RETRY_STARTED, RETRY_RESUME_STARTED, RETRY_SUCCEEDED,
        RETRY_FAILED, SESSION_INVALIDATED
    }

    public static GatewayStateChange connected() {
        return new GatewayStateChange(State.CONNECTED, 0, null);
    }

    public static ClosingStateChange disconnected(DisconnectBehavior behavior, CloseStatus status) {
        return new ClosingStateChange(behavior, status);
    }

    public static GatewayStateChange disconnectedResume() {
        return new GatewayStateChange(State.DISCONNECTED_RESUME, 0, null);
    }

    public static GatewayStateChange retryStarted(Duration nextAttemptBackoff) {
        return new GatewayStateChange(State.RETRY_STARTED, 1, nextAttemptBackoff);
    }

    public static GatewayStateChange retryStartedResume(Duration nextAttemptBackoff) {
        return new GatewayStateChange(State.RETRY_RESUME_STARTED, 1, nextAttemptBackoff);
    }

    public static GatewayStateChange retrySucceeded(long currentAttempt) {
        return new GatewayStateChange(State.RETRY_SUCCEEDED, currentAttempt, null);
    }

    public static GatewayStateChange retryFailed(long currentAttempt, Duration nextAttemptBackoff) {
        return new GatewayStateChange(State.RETRY_FAILED, currentAttempt, nextAttemptBackoff);
    }

    public static GatewayStateChange sessionInvalidated() {
        return new GatewayStateChange(State.SESSION_INVALIDATED, 0, null);
    }

    private final State state;
    private final long currentAttempt;
    @Nullable
    private final Duration backoff;

    protected GatewayStateChange(State state, long currentAttempt, @Nullable Duration backoff) {
        this.state = state;
        this.currentAttempt = currentAttempt;
        this.backoff = backoff;
    }

    public State getState() {
        return state;
    }

    public long getCurrentAttempt() {
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
                ", currentAttempt=" + currentAttempt +
                ", backoff=" + backoff +
                ']';
    }
}
