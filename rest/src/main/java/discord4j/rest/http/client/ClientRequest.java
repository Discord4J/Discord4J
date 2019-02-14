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

import discord4j.rest.request.DiscordRequest;
import discord4j.rest.route.Route;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

/**
 * An adapted request definition from an original {@link DiscordRequest}.
 */
public class ClientRequest {

    private final DiscordRequest<?> request;
    private final String url;
    private final HttpHeaders headers;

    public ClientRequest(DiscordRequest<?> request, String url, HttpHeaders headers) {
        this.request = request;
        this.url = url;
        this.headers = headers;
    }

    public HttpMethod method() {
        return request.getRoute().getMethod();
    }

    public String url() {
        return url;
    }

    public HttpHeaders headers() {
        return headers;
    }

    public DiscordRequest<?> getDiscordRequest() {
        return request;
    }

    public Route<?> getRoute() {
        return request.getRoute();
    }

    @Override
    public String toString() {
        return "ClientRequest{" +
                "method=" + method() +
                ", url='" + url + '\'' +
                ", headers=" + headers.copy().remove(HttpHeaderNames.AUTHORIZATION).toString() +
                '}';
    }
}
