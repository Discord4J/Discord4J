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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.rest.request;

import reactor.core.publisher.Flux;

/**
 * Abstraction for a REST request queue.
 *
 * @param <T> the type of queue elements materializing the requests.
 */
public interface RequestQueue<T> {

    /**
     * Pushes a new request to the queue.
     *
     * @param request the request to push.
     * @return {@code true} if the request was submitted successfully, {@code false} otherwise
     */
    boolean push(T request);

    /**
     * Exposes a Flux that continuously emits requests available in queue.
     *
     * @return a Flux of requests.
     */
    Flux<T> requests();
}
