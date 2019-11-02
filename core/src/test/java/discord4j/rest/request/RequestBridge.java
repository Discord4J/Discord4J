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

import reactor.core.publisher.MonoProcessor;

class RequestBridge<T> {

    private final String request;
    private final MonoProcessor<T> acquire;
    private final MonoProcessor<T> release;

    public RequestBridge(String request, MonoProcessor<T> acquire, MonoProcessor<T> release) {
        this.request = request;
        this.acquire = acquire;
        this.release = release;
    }

    public String getRequest() {
        return request;
    }

    public MonoProcessor<T> getAcquire() {
        return acquire;
    }

    public MonoProcessor<T> getRelease() {
        return release;
    }
}
