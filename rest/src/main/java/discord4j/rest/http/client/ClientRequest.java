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
import discord4j.rest.util.RouteUtils;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Optional;

/**
 * An adapted request definition from an original {@link DiscordRequest}.
 */
public class ClientRequest {

    private final DiscordRequest<?> request;
    private final String url;
    private final HttpHeaders headers;

    /**
     * Create a new {@link ClientRequest} from the given request template.
     *
     * @param request the {@link DiscordRequest} template
     */
    public ClientRequest(DiscordRequest<?> request) {
        this.request = request;
        this.url = RouteUtils.expandQuery(request.getCompleteUri(), request.getQueryParams());
        this.headers = Optional.ofNullable(request.getHeaders())
                .map(map -> map.entrySet().stream()
                        .reduce((HttpHeaders) new DefaultHttpHeaders(), (headers, entry) -> {
                            String key = entry.getKey();
                            entry.getValue().forEach(value -> headers.add(key, value));
                            return headers;
                        }, HttpHeaders::add))
                .orElse(new DefaultHttpHeaders());
    }

    /**
     * Return the HTTP method.
     *
     * @return the {@link HttpMethod} of this {@link ClientRequest}
     */
    public HttpMethod getMethod() {
        return request.getRoute().getMethod();
    }

    /**
     * Return the request URL.
     *
     * @return the request URL for this {@link ClientRequest}
     */
    public String getUrl() {
        return url;
    }

    /**
     * Return the headers of this request.
     *
     * @return the {@link HttpHeaders} of this {@link ClientRequest}
     */
    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Return the original request template.
     *
     * @return the {@link DiscordRequest} template that created this {@link ClientRequest}
     */
    public DiscordRequest<?> getDiscordRequest() {
        return request;
    }

    /**
     * Return the API endpoint targeted by this request.
     *
     * @return the {@link Route} requested by this {@link ClientRequest}
     */
    public Route<?> getRoute() {
        return request.getRoute();
    }

    @Override
    public String toString() {
        return "ClientRequest{" +
                "method=" + getMethod() +
                ", url='" + url + '\'' +
                ", headers=" + headers.copy().remove(HttpHeaderNames.AUTHORIZATION).toString() +
                '}';
    }
}
