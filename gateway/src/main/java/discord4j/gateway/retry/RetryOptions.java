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

import reactor.retry.Backoff;
import reactor.retry.BackoffDelay;
import reactor.retry.Jitter;

import java.time.Duration;

/**
 * A configuration object to customize the gateway retry (reconnecting) policy.
 */
public class RetryOptions {

    private final RetryContext retryContext;
    private final int maxRetries;

    /**
     * Create a retry configuration object.
     *
     * @param firstBackoff the Duration to backoff on first attempts
     * @param maxBackoffInterval the maximum backoff Duration
     * @param maxRetries the maximum amount of retries to attempt a reconnect
     */
    public RetryOptions(Duration firstBackoff, Duration maxBackoffInterval, int maxRetries) {
        if (firstBackoff.minus(Duration.ofSeconds(2)).isNegative()) {
            throw new IllegalArgumentException("firstBackoff duration must be at least 2 seconds");
        }
        if (maxBackoffInterval.minus(firstBackoff).isNegative()) {
            throw new IllegalArgumentException("maxBackoffInterval must be at least the same as firstBackoff");
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be a positive integer");
        }
        this.retryContext = new RetryContext(firstBackoff, maxBackoffInterval);
        this.maxRetries = maxRetries;
    }

    /**
     * Retrieve a stateful context object to hold current attempt count and backoff delay on each retry.
     *
     * @return the RetryContext associated with this configuration object
     */
    public RetryContext getRetryContext() {
        return retryContext;
    }

    /**
     * Retrieve the backoff function used for retrying. It uses a RetryContext object to calculate the correct backoff
     * delay.
     *
     * @return a Backoff function
     */
    public Backoff getBackoff() {
        return context -> {
            RetryContext appContext = (RetryContext) context.applicationContext();
            Duration nextBackoff;
            try {
                long factor = (long) Math.pow(2, (appContext.getAttempts() - 1));
                nextBackoff = appContext.getFirstBackoff().multipliedBy(factor);
            } catch (ArithmeticException e) {
                nextBackoff = appContext.getMaxBackoffInterval();
            }
            return new BackoffDelay(appContext.getFirstBackoff(), appContext.getMaxBackoffInterval(), nextBackoff);
        };
    }

    /**
     * Retrieve the jitter to be applied on each backoff delay.
     *
     * @return a Jitter function
     */
    public Jitter getJitter() {
        return Jitter.random();
    }

    /**
     * Returns the number of retries.
     *
     * @return number of retries
     */
    public int getMaxRetries() {
        return maxRetries;
    }
}
