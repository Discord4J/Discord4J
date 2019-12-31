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

package discord4j.rest.request;

import reactor.netty.http.client.HttpClientResponse;

import java.time.Duration;

/**
 * A mapper between a {@link HttpClientResponse} and a {@link Duration} representing a delay due to rate limiting.
 */
@FunctionalInterface
public interface RateLimitStrategy {

    /**
     * Apply this function to a {@link HttpClientResponse} to obtain a {@link Duration} representing a delay due to
     * rate limiting.
     *
     * @param response the original {@link HttpClientResponse}
     * @return a {@link Duration} indicating rate limiting, can be {@link Duration#ZERO} if no limit is present
     */
    Duration apply(HttpClientResponse response);
}
