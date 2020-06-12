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
import java.util.function.Supplier;

/**
 * A configuration object to customize the gateway reconnection policy.
 */
public class ReconnectOptions {

    public static final Supplier<Scheduler> DEFAULT_BACKOFF_SCHEDULER = () ->
            Schedulers.newParallel("d4j-backoff", Schedulers.DEFAULT_POOL_SIZE, true);

    private final Duration firstBackoff;
    private final Duration maxBackoffInterval;
    private final long maxRetries;
    private final Backoff backoff;
    private final Jitter jitter;
    private final Scheduler backoffScheduler;
    private final double jitterFactor;

    protected ReconnectOptions(Builder builder) {
        this.firstBackoff = Objects.requireNonNull(builder.firstBackoff, "firstBackoff");
        this.maxBackoffInterval = Objects.requireNonNull(builder.maxBackoffInterval, "maxBackoffInterval");
        this.maxRetries = builder.maxRetries;
        this.backoff = Objects.requireNonNull(builder.backoff, "backoff");
        this.jitter = Objects.requireNonNull(builder.jitter, "jitter");
        if (builder.backoffScheduler == null) {
            this.backoffScheduler = DEFAULT_BACKOFF_SCHEDULER.get();
        } else {
            this.backoffScheduler = builder.backoffScheduler;
        }
        this.jitterFactor = builder.jitterFactor;
    }

    /**
     * Create a default {@link ReconnectOptions}.
     *
     * @return a new reconnect options configured with all defaults
     */
    public static ReconnectOptions create() {
        return new Builder().build();
    }

    /**
     * Create a new builder for {@link ReconnectOptions}.
     *
     * @return a new builder
     */
    public static ReconnectOptions.Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Duration firstBackoff = Duration.ofSeconds(2);
        private Duration maxBackoffInterval = Duration.ofSeconds(30);
        private long maxRetries = Long.MAX_VALUE;
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
        private Scheduler backoffScheduler = null;
        private double jitterFactor = 0.5d;

        protected Builder() {
        }

        /**
         * Set the first {@link Duration} to be applied when computing a backoff. Defaults to 2 seconds.
         *
         * @param firstBackoff the minimum duration to be applied as backoff
         * @return this builder
         */
        public Builder setFirstBackoff(Duration firstBackoff) {
            if (firstBackoff.minus(Duration.ofSeconds(2)).isNegative()) {
                throw new IllegalArgumentException("firstBackoff duration must be at least 2 seconds");
            }
            this.firstBackoff = firstBackoff;
            return this;
        }

        /**
         * Set the maximum {@link Duration} to be applied when computing a backoff. Defaults to 30 seconds.
         *
         * @param maxBackoffInterval the maximum duration to be applied as backoff
         * @return this builder
         */
        public Builder setMaxBackoffInterval(Duration maxBackoffInterval) {
            if (maxBackoffInterval.minus(firstBackoff).isNegative()) {
                throw new IllegalArgumentException("maxBackoffInterval must be at least the same as firstBackoff");
            }
            this.maxBackoffInterval = maxBackoffInterval;
            return this;
        }

        /**
         * Set the maximum number of iterations to retry before rethrowing the error as exhausted attempts. Defaults
         * to Long.MAX_VALUE (unlimited retries).
         *
         * @param maxRetries the maximum number of retries
         * @return this builder
         */
        public Builder setMaxRetries(long maxRetries) {
            if (maxRetries < 0) {
                throw new IllegalArgumentException("maxRetries must be a positive integer");
            }
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Set the backoff function given by reactor-extra {@link Backoff} type. Defaults to an exponential backoff
         * strategy that uses a context object for obtaining the actual iteration.
         *
         * @param backoff a backoff function to apply on retries
         * @return this builder
         * @deprecated only select implementations will use this value. Moving forward, consider assuming an
         * exponential backoff function bounded by {@link #getFirstBackoff()} and {@link #getMaxBackoffInterval()}
         */
        @Deprecated
        public Builder setBackoff(Backoff backoff) {
            this.backoff = backoff;
            return this;
        }

        /**
         * Set the jitter function given by reactor-extra {@link Jitter} type. Defaults to 50% randomness.
         *
         * @param jitter a jitter function to apply on retries
         * @return this builder
         * @deprecated only select implementations will use this value. Moving forward, consider using
         * {@link #setJitterFactor(double)}}
         */
        @Deprecated
        public Builder setJitter(Jitter jitter) {
            this.jitter = jitter;
            return this;
        }

        /**
         * Set the {@link Scheduler} to be used when building delayed sequences as backoff. Defaults to dedicated
         * parallel scheduler {@link #DEFAULT_BACKOFF_SCHEDULER}.
         *
         * @param backoffScheduler a reactor scheduler used for backoff delays
         * @return this builder
         */
        public Builder setBackoffScheduler(Scheduler backoffScheduler) {
            this.backoffScheduler = backoffScheduler;
            return this;
        }

        /**
         * Set a jitter factor for exponential backoff that adds randomness to each backoff. Defaults to {@code 0.5}
         * (a jitter of at most 50% of the computed delay)
         *
         * @param jitterFactor the new jitter factor as a {@code double} between {@code 0d} and {@code 1d}
         * @return this builder
         */
        public Builder setJitterFactor(double jitterFactor) {
            if (jitterFactor < 0d || jitterFactor > 1d) {
                throw new IllegalArgumentException("Invalid jitter factor value");
            }
            this.jitterFactor = jitterFactor;
            return this;
        }

        public ReconnectOptions build() {
            return new ReconnectOptions(this);
        }
    }

    /**
     * Return the minimum backoff duration.
     *
     * @return minimum backoff duration
     */
    public Duration getFirstBackoff() {
        return firstBackoff;
    }

    /**
     * Return the maximum backoff duration.
     *
     * @return maximum backoff duration
     */
    public Duration getMaxBackoffInterval() {
        return maxBackoffInterval;
    }

    /**
     * Returns the number of retries.
     *
     * @return number of retries
     */
    public long getMaxRetries() {
        return maxRetries;
    }

    /**
     * Retrieve the backoff function used for retrying. It uses a RetryContext object to calculate the correct backoff
     * delay.
     *
     * @return a Backoff function
     * @deprecated only select implementations will use this value. Moving forward, consider assuming an exponential
     * backoff function bounded by {@link #getFirstBackoff()} and {@link #getMaxBackoffInterval()}
     */
    @Deprecated
    public Backoff getBackoff() {
        return backoff;
    }

    /**
     * Retrieve the jitter to be applied on each backoff delay.
     *
     * @return a Jitter function
     * @deprecated only select implementations will use this value. Moving forward, consider also using
     * {@link #getJitterFactor()}
     */
    @Deprecated
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

    /**
     * Retrieve the jitter factor to be applied on each backoff delay.
     *
     * @return a jitter factor value between {@code 0d} and {@code 1d}
     */
    public double getJitterFactor() {
        return jitterFactor;
    }
}
