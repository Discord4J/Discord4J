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
import discord4j.rest.util.Multimap;
import discord4j.rest.util.RouteUtils;
import reactor.util.annotation.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * Template encoding all of the needed information to make an HTTP request to Discord.
 */
public class DiscordWebRequest {

    private final Route route;
    private final String completeUri;
    private final Map<String, String> uriVariableMap;

    @Nullable
    private Object body;

    @Nullable
    private Multimap<String, Object> queryParams;

    @Nullable
    private Map<String, Set<String>> headers;

    /**
     * Create a new {@link DiscordWebRequest} template based on a {@link Route} and its compiled URI.
     *
     * @param route the API resource targeted by this request
     * @param uriVars the values to expand each template parameter
     */
    public DiscordWebRequest(Route route, Object... uriVars) {
        this.route = route;
        this.completeUri = RouteUtils.expand(route.getUriTemplate(), uriVars);
        this.uriVariableMap = RouteUtils.createVariableMap(route.getUriTemplate(), uriVars);
    }

    /**
     * Return the API endpoint targeted by this request.
     *
     * @return the {@link Route} of this {@link DiscordWebRequest}
     */
    public Route getRoute() {
        return route;
    }

    /**
     * Return the compiled URI of this request.
     *
     * @return the compiled URI, containing the actual path variables
     */
    public String getCompleteUri() {
        return completeUri;
    }

    /**
     * Return the body of this request, if present.
     *
     * @return the body of this request, or {@code null} if this request carries no HTTP body
     */
    @Nullable
    public Object getBody() {
        return body;
    }

    /**
     * Return the query parameters saved in this request, if present.
     *
     * @return a map representing query parameters, or {@code null} if none are defined
     */
    @Nullable
    public Multimap<String, Object> getQueryParams() {
        return queryParams;
    }

    /**
     * Return the request headers, if present.
     *
     * @return a map representing HTTP headers, or {@code null} if none are defined
     */
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
    public DiscordWebRequest body(Object body) {
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
    public DiscordWebRequest query(String key, Object value) {
        if (queryParams == null) {
            queryParams = new Multimap<>();
        }
        queryParams.add(key, value);
        return this;
    }

    /**
     * Adds the given names and values as request query parameters.
     *
     * @param params a map of query parameter names to values
     * @return this request
     */
    public DiscordWebRequest query(Map<String, Object> params) {
        params.forEach(this::query);
        return this;
    }

    /**
     * Add the given names and values as request query parameters.
     *
     * @param params a map of query parameter names to values
     * @return this request
     */
    public DiscordWebRequest query(Multimap<String, Object> params) {
        params.forEachElement(this::query);
        return this;
    }

    /**
     * Adds the given key and value to the headers of this request.
     *
     * @param key the header key
     * @param value the header value
     * @return this request
     */
    public DiscordWebRequest header(String key, String value) {
        if (headers == null) {
            headers = new LinkedHashMap<>();
        }
        headers.computeIfAbsent(key.toLowerCase(), k -> new LinkedHashSet<>()).add(value);
        return this;
    }

    /**
     * Adds the given key and value to the headers of this request
     * if and only if {@code value} is not {@code null}.
     *
     * @param key the header key
     * @param value the header value
     * @return this request
     */
    public DiscordWebRequest optionalHeader(String key, @Nullable String value) {
        return (value == null) ? this : header(key, value);
    }

    boolean matchesVariables(Predicate<Map<String, String>> matcher) {
        return matcher.test(uriVariableMap);
    }

    /**
     * Exchange this request through the given {@link Router}.
     *
     * @param router a router that performs this request
     * @return the result of this request
     */
    public DiscordWebResponse exchange(Router router) {
        return router.exchange(this);
    }

    public String getDescription() {
        return route.getMethod() + " " + completeUri;
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
