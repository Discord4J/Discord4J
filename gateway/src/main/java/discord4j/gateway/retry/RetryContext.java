package discord4j.gateway.retry;

import java.time.Duration;

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

	public void next() {
		connected = false;
		attempts++;
	}

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
