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
public class TokenBucket implements GatewayLimiter {

    private final long capacity;
    private final double refillTokensPerOneMillis;
    private final long refillTokens;
    private final long refillPeriodMillis;

    private volatile double availableTokens;
    private volatile long lastRefillTimestamp;

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

    @Override
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
        if (lastRefillTimestamp == 0) {
            this.lastRefillTimestamp = currentTimeMillis;
        } else if (currentTimeMillis > lastRefillTimestamp) {
            long millisSinceLastRefill = currentTimeMillis - lastRefillTimestamp;
            double refill = millisSinceLastRefill * refillTokensPerOneMillis;
            this.availableTokens = Math.min(capacity, availableTokens + refill);
            this.lastRefillTimestamp = currentTimeMillis;
        }
    }

    @Override
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
