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

package discord4j.common;

/**
 * Represents a rate-limiting strategy that can be shared across shards.
 */
public interface RateLimiter {

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
    boolean tryConsume(int numberTokens);

    /**
     * Calculate the time (in milliseconds) a consumer should delay a call to {@link #tryConsume(int)} in order to be
     * successful.
     *
     * @param tokens the number of permits sought to consume
     * @return delay in milliseconds that a consumer must wait to consume <code>tokens</code> amount of permits.
     */
    long delayMillisToConsume(long tokens);
}
