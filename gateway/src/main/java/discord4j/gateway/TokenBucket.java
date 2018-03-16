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

package discord4j.gateway;

import java.time.Duration;

/**
 * A minimalistic implementation of the token bucket algorithm.
 *
 * @see <a href="https://github.com/vladimir-bukhtoyarov/bucket4j">Bucket4j Project</a>
 */
public class TokenBucket {

    private final long capacity;
    private final double refillTokensPerOneMillis;
    private final long refillTokens;
    private final long refillPeriodMillis;

    private double availableTokens;
    private long lastRefillTimestamp;

    /**
     * Creates token-bucket with specified capacity and refill rate
     *
     * @param capacity the number of tokens to hold
     * @param refillPeriod the refill period to add up to <code>capacity</code> number of tokens
     */
    public TokenBucket(long capacity, Duration refillPeriod) {
        this.capacity = capacity;
        this.refillTokens = capacity;
        this.refillPeriodMillis = refillPeriod.toMillis();
        this.refillTokensPerOneMillis = (double) capacity / (double) refillPeriodMillis;

        this.availableTokens = capacity;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    /**
     * Attempt to consume a given number of permits from this bucket.
     * <p>
     * This method does not block or incur in any waiting step. If no permits are available, this method will simply
     * return <code>false</code>. To obtain the delay needed to wait in order to consume the next tokens, see
     * {@link #delayMillisToConsume(long)}.
     *
     * @param numberTokens the permits to consume
     * @return <code>true</code> if it was successful, <code>false</code> otherwise
     */
    public synchronized boolean tryConsume(int numberTokens) {
        refill();
        if (availableTokens < numberTokens) {
            return false;
        } else {
            availableTokens -= numberTokens;
            return true;
        }
    }

    private void refill() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis > lastRefillTimestamp) {
            long millisSinceLastRefill = currentTimeMillis - lastRefillTimestamp;
            double refill = millisSinceLastRefill * refillTokensPerOneMillis;
            this.availableTokens = Math.min(capacity, availableTokens + refill);
            this.lastRefillTimestamp = currentTimeMillis;
        }
    }

    /**
     * Calculate the time (in milliseconds) a consumer should delay a call to {@link #tryConsume(int)} in order to be
     * successful.
     *
     * @param tokens the number of permits sought to consume
     * @return delay in milliseconds that a consumer must wait to consume <code>tokens</code> amount of permits.
     */
    public long delayMillisToConsume(long tokens) {
        long currentSize = (long) availableTokens;
        if (tokens <= currentSize) {
            return 0;
        }
        long deficit = tokens - currentSize;
        long refillPeriodMillis = this.refillPeriodMillis;
        long refillPeriodTokens = refillTokens;

        long divided = multiplyExactOrReturnMaxValue(refillPeriodMillis, deficit);
        if (divided == Long.MAX_VALUE) {
            return (long) ((double) deficit / (double) refillPeriodTokens * (double) refillPeriodMillis);
        } else {
            return divided / refillPeriodTokens;
        }
    }

    private static long multiplyExactOrReturnMaxValue(long x, long y) {
        long r = x * y;
        long ax = Math.abs(x);
        long ay = Math.abs(y);
        if (((ax | ay) >>> 31 != 0)) {
            if (((y != 0) && (r / y != x)) || (x == Long.MIN_VALUE && y == -1)) {
                return Long.MAX_VALUE;
            }
        }
        return r;
    }

}
