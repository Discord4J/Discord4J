package discord4j.gateway.retry;

import java.time.Duration;

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

	private boolean connected = false;
	private int attempts = 1;
	private int resetCount = 0;

	public RetryContext(Duration firstBackoff, Duration maxBackoffInterval) {
		this.firstBackoff = firstBackoff;
		this.maxBackoffInterval = maxBackoffInterval;
	}

	/**
	 * Signal that the next retry attempt should be underway.
	 */
	public void next() {
		connected = false;
		attempts++;
	}

	/**
	 * Reset the attempt count, treating further calls to {@link #next()} as new retry sequences.
	 */
	public void reset() {
		connected = true;
		attempts = 1;
		resetCount++;
	}

	public Duration getFirstBackoff() {
		return firstBackoff;
	}

	public Duration getMaxBackoffInterval() {
		return maxBackoffInterval;
	}

	public boolean isConnected() {
		return connected;
	}

	public int getAttempts() {
		return attempts;
	}

	public int getResetCount() {
		return resetCount;
	}
}
