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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.gateway.retry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Encapsulate retrying state for reconnect operations.
 * <p>
 * Used as context for {@link reactor.retry.Retry} calls, keeps track of the current retry attempt for backoff
 * calculations (through {@link #next()} and restarting the attempt count once a retry has succeeded (through
 * {@link #reset()}).
 */
public class RetryContext {

    private final Duration firstBackoff;
    private final Duration maxBackoffInterval;

    private AtomicBoolean connected = new AtomicBoolean(false);
    private AtomicInteger attempts = new AtomicInteger(1);
    private AtomicInteger resetCount = new AtomicInteger(0);

    public RetryContext(Duration firstBackoff, Duration maxBackoffInterval) {
        this.firstBackoff = firstBackoff;
        this.maxBackoffInterval = maxBackoffInterval;
    }

    /**
     * Signal that the next retry attempt should be underway.
     */
    public void next() {
        connected.compareAndSet(true, false);
        attempts.incrementAndGet();
    }

    /**
     * Reset the attempt count, treating further calls to {@link #next()} as new retry sequences.
     */
    public void reset() {
        connected.compareAndSet(false, true);
        attempts.set(1);
        resetCount.incrementAndGet();
    }

    public Duration getFirstBackoff() {
        return firstBackoff;
    }

    public Duration getMaxBackoffInterval() {
        return maxBackoffInterval;
    }

    public boolean isConnected() {
        return connected.get();
    }

    public int getAttempts() {
        return attempts.get();
    }

    public int getResetCount() {
        return resetCount.get();
    }
}
