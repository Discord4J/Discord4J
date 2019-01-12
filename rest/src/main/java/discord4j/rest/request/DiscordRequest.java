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

import discord4j.rest.route.Route;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Encodes all of the needed information to make an HTTP request to Discord.
 *
 * @param <T> The response type.
 * @since 3.0
 */
public class DiscordRequest<T> {

    private final Route<T> route;
    private final String completeUri;

    @Nullable
    private Object body;

    @Nullable
    private Map<String, Object> queryParams;

    @Nullable
    private Map<String, Set<String>> headers;

    public DiscordRequest(Route<T> route, String completeUri) {
        this.route = route;
        this.completeUri = completeUri;
    }

    Route<T> getRoute() {
        return route;
    }

    String getCompleteUri() {
        return completeUri;
    }

    @Nullable
    public Object getBody() {
        return body;
    }

    @Nullable
    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    @Nullable
    public Map<String, Set<String>> getHeaders() {
        return headers;
    }

    /**
     * Set the given synchronous {@link java.lang.Object} as the body for the request.
     *
     * @param body the object to set as request body
     * @return this request
     */
    public DiscordRequest<T> body(Object body) {
        this.body = body;
        return this;
    }

    /**
     * Add the given name and value as a request query parameter.
     *
     * @param key the query parameter name
     * @param value the query parameter value
     * @return this request
     */
    public DiscordRequest<T> query(String key, Object value) {
        if (queryParams == null) {
            queryParams = new LinkedHashMap<>();
        }
        queryParams.put(key, value);
        return this;
    }

    /**
     * Adds the given names and values as request query parameters.
     *
     * @param params a map of query parameter names to values
     * @return this request
     */
    public DiscordRequest<T> query(Map<String, Object> params) {
        params.forEach(this::query);
        return this;
    }

    /**
     * Adds the given key and value to the headers of this request.
     *
     * @param key the header key
     * @param value the header value
     * @return this request
     */
    public DiscordRequest<T> header(String key, String value) {
        if (headers == null) {
            headers = new LinkedHashMap<>();
        }
        headers.computeIfAbsent(key.toLowerCase(), k -> new LinkedHashSet<>()).add(value);
        return this;
    }

    /**
     * Exchange this request through the given {@link Router}.
     *
     * @param router a router that performs this request
     * @return the result of this request
     */
    public Mono<T> exchange(Router router) {
        return router.exchange(this);
    }

    @Override
    public String toString() {
        return "DiscordRequest{" +
                "route=" + route +
                ", completeUri='" + completeUri + '\'' +
                ", body=" + body +
                ", queryParams=" + queryParams +
                ", headers=" + headers +
                '}';
    }
}
