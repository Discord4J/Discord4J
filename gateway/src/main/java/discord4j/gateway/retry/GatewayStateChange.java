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

import discord4j.discordjson.json.gateway.Dispatch;
import reactor.util.annotation.Nullable;

import java.time.Duration;

public class GatewayStateChange implements Dispatch {

    public enum State {
        CONNECTED, DISCONNECTED, DISCONNECTED_RESUME, RETRY_STARTED, RETRY_RESUME_STARTED, RETRY_SUCCEEDED, RETRY_FAILED
    }

    public static GatewayStateChange connected() {
        return new GatewayStateChange(State.CONNECTED, 0, null);
    }

    public static GatewayStateChange disconnected() {
        return new GatewayStateChange(State.DISCONNECTED, 0, null);
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

    public static GatewayStateChange retrySucceeded(int currentAttempt) {
        return new GatewayStateChange(State.RETRY_SUCCEEDED, currentAttempt, null);
    }

    public static GatewayStateChange retryFailed(int currentAttempt, Duration nextAttemptBackoff) {
        return new GatewayStateChange(State.RETRY_FAILED, currentAttempt, nextAttemptBackoff);
    }

    private final State state;
    private final int currentAttempt;
    private final Duration backoff;

    private GatewayStateChange(State state, int currentAttempt, @Nullable Duration backoff) {
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
                ", currentAttempt=" + currentAttempt +
                ", backoff=" + backoff +
                ']';
    }
}
