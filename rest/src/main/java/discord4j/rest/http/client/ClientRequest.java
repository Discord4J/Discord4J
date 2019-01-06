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

package discord4j.rest.http.client;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

/**
 * A wrapper over a partial HTTP client request definition.
 */
public class ClientRequest {

    private final HttpMethod method;
    private final String url;
    private final HttpHeaders headers;

    public ClientRequest(HttpMethod method, String url, HttpHeaders headers) {
        this.method = method;
        this.url = url;
        this.headers = headers;
    }

    public HttpMethod method() {
        return method;
    }

    public String url() {
        return url;
    }

    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public String toString() {
        return "ClientRequest{" +
                "method=" + method +
                ", url='" + url + '\'' +
                ", headers=" + headers +
                '}';
    }
}
