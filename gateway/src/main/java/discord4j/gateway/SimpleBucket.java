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
import java.util.concurrent.atomic.AtomicLong;

public class SimpleBucket implements GatewayLimiter {

    private final long capacity;
    private final long refillPeriodMillis;

    private final AtomicLong count;
    private final AtomicLong nextRefillAt;

    public SimpleBucket(long capacity, Duration refillPeriod) {
        this.capacity = capacity;
        this.refillPeriodMillis = refillPeriod.toMillis();
        this.count = new AtomicLong(0);
        this.nextRefillAt = new AtomicLong(0);
    }

    @Override
    public synchronized boolean tryConsume(int numberTokens) {
        long now = System.currentTimeMillis();
        if (nextRefillAt.get() <= now) {
            count.set(0);
            nextRefillAt.set(now + refillPeriodMillis);
        }
        if (count.get() + numberTokens <= capacity) {
            count.addAndGet(numberTokens);
            return true;
        }
        return false;
    }

    @Override
    public synchronized long delayMillisToConsume(long tokens) {
        if (count.get() + tokens <= capacity) {
            return 0;
        }
        long now = System.currentTimeMillis();
        long refills = (long) Math.ceil((tokens / (double) capacity) - 1);
        return (nextRefillAt.get() - now) + (refillPeriodMillis * refills);
    }

}
