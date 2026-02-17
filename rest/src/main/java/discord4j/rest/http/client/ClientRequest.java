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

import discord4j.rest.request.DiscordWebRequest;
import discord4j.rest.route.Route;
import discord4j.rest.util.RouteUtils;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * An adapted request definition from an original {@link DiscordWebRequest}.
 */
public class ClientRequest {

    private final String id;
    private final DiscordWebRequest request;
    private final String url;
    private final HttpHeaders headers;
    private final Object body;

    @Nullable
    private final AuthorizationScheme authorizationScheme;

    @Nullable
    private final String authorizationValue;

    /**
     * Create a new {@link ClientRequest} from the given request template.
     *
     * @param request the {@link DiscordWebRequest} template
     */
    public ClientRequest(DiscordWebRequest request) {
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
        this.body = request.getBody();
        this.id = Integer.toHexString(System.identityHashCode(this));
        this.authorizationScheme = request.getAuthorizationScheme();
        this.authorizationValue = request.getAuthorizationValue();
    }

    /**
     * Return this request's ID for correlation.
     *
     * @return this request's ID
     */
    public String getId() {
        return id;
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
     * Return the body to encode while processing this request.
     *
     * @return the request body, can be {@code null}
     */
    @Nullable
    public Object getBody() {
        return body;
    }

    /**
     * Return the original request template.
     *
     * @return the {@link DiscordWebRequest} template that created this {@link ClientRequest}
     */
    public DiscordWebRequest getDiscordRequest() {
        return request;
    }

    /**
     * Return the API endpoint targeted by this request.
     *
     * @return the {@link Route} requested by this {@link ClientRequest}
     */
    public Route getRoute() {
        return request.getRoute();
    }

    public String getDescription() {
        return request.getDescription();
    }

    @Nullable
    public AuthorizationScheme getAuthorizationScheme() {
        return authorizationScheme;
    }

    @Nullable
    public String getAuthorizationValue() {
        return authorizationValue;
    }

    @Override
    public String toString() {
        return "ClientRequest{" +
                "method=" + getMethod() +
                ", url='" + url + '\'' +
                ", headers=" + headers.copy().remove(HttpHeaderNames.AUTHORIZATION).toString() +
                ", body=" + body +
                ", id=" + id +
                '}';
    }
}
