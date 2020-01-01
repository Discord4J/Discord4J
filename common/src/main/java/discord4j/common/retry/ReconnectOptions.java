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

package discord4j.common.retry;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.retry.Backoff;
import reactor.retry.BackoffDelay;
import reactor.retry.Jitter;

import java.time.Duration;
import java.util.Objects;

/**
 * A configuration object to customize the gateway reconnection policy.
 */
public class ReconnectOptions {

    private final Duration firstBackoff;
    private final Duration maxBackoffInterval;
    private final int maxRetries;
    private final Backoff backoff;
    private final Jitter jitter;
    private final Scheduler backoffScheduler;

    protected ReconnectOptions(Builder builder) {
        this.firstBackoff = Objects.requireNonNull(builder.firstBackoff, "firstBackoff");
        this.maxBackoffInterval = Objects.requireNonNull(builder.maxBackoffInterval, "maxBackoffInterval");
        this.maxRetries = builder.maxRetries;
        this.backoff = Objects.requireNonNull(builder.backoff, "backoff");
        this.jitter = Objects.requireNonNull(builder.jitter, "jitter");
        this.backoffScheduler = Objects.requireNonNull(builder.backoffScheduler, "backoffScheduler");
    }

    public static ReconnectOptions create() {
        return new Builder().build();
    }

    public static ReconnectOptions.Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Duration firstBackoff = Duration.ofSeconds(2);
        private Duration maxBackoffInterval = Duration.ofSeconds(120);
        private int maxRetries = Integer.MAX_VALUE;
        private Backoff backoff = context -> {
            ReconnectContext appContext = (ReconnectContext) context.applicationContext();
            Duration nextBackoff;
            try {
                long factor = (long) Math.pow(2, (appContext.getAttempts() - 1));
                nextBackoff = appContext.getFirstBackoff().multipliedBy(factor);
            } catch (ArithmeticException e) {
                nextBackoff = appContext.getMaxBackoffInterval();
            }
            return new BackoffDelay(appContext.getFirstBackoff(), appContext.getMaxBackoffInterval(), nextBackoff);
        };
        private Jitter jitter = Jitter.random();
        private Scheduler backoffScheduler = Schedulers.parallel();

        protected Builder() {
        }

        public Builder setFirstBackoff(Duration firstBackoff) {
            if (firstBackoff.minus(Duration.ofSeconds(2)).isNegative()) {
                throw new IllegalArgumentException("firstBackoff duration must be at least 2 seconds");
            }
            this.firstBackoff = firstBackoff;
            return this;
        }

        public Builder setMaxBackoffInterval(Duration maxBackoffInterval) {
            if (maxBackoffInterval.minus(firstBackoff).isNegative()) {
                throw new IllegalArgumentException("maxBackoffInterval must be at least the same as firstBackoff");
            }
            this.maxBackoffInterval = maxBackoffInterval;
            return this;
        }

        public Builder setMaxRetries(int maxRetries) {
            if (maxRetries < 0) {
                throw new IllegalArgumentException("maxRetries must be a positive integer");
            }
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder setBackoff(Backoff backoff) {
            this.backoff = backoff;
            return this;
        }

        public Builder setJitter(Jitter jitter) {
            this.jitter = jitter;
            return this;
        }

        public Builder setBackoffScheduler(Scheduler backoffScheduler) {
            this.backoffScheduler = backoffScheduler;
            return this;
        }

        public ReconnectOptions build() {
            return new ReconnectOptions(this);
        }
    }

    public Duration getFirstBackoff() {
        return firstBackoff;
    }

    public Duration getMaxBackoffInterval() {
        return maxBackoffInterval;
    }

    /**
     * Returns the number of retries.
     *
     * @return number of retries
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Retrieve the backoff function used for retrying. It uses a RetryContext object to calculate the correct backoff
     * delay.
     *
     * @return a Backoff function
     */
    public Backoff getBackoff() {
        return backoff;
    }

    /**
     * Retrieve the jitter to be applied on each backoff delay.
     *
     * @return a Jitter function
     */
    public Jitter getJitter() {
        return jitter;
    }

    /**
     * Returns a scheduler provided every reconnect attempt, as backoff delay.
     *
     * @return scheduler used when reconnecting
     */
    public Scheduler getBackoffScheduler() {
        return backoffScheduler;
    }
}
