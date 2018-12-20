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
package discord4j.rest.route;

import discord4j.rest.request.DiscordRequest;
import discord4j.rest.util.RouteUtils;
import io.netty.handler.codec.http.HttpMethod;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Provides a mapping between a Discord API endpoint and its response type.
 *
 * @param <T> the response type
 * @since 3.0
 */
public class Route<T> {

    private final HttpMethod method;
    private final String uriTemplate;
    private final Class<T> responseType;

    private Route(HttpMethod method, String uriTemplate, Class<T> responseType) {
        this.method = method;
        this.uriTemplate = uriTemplate;
        this.responseType = responseType;
    }

    public static <T> Route<T> get(String uri, Class<T> responseType) {
        return new Route<>(HttpMethod.GET, uri, responseType);
    }

    public static <T> Route<T> post(String uri, Class<T> responseType) {
        return new Route<>(HttpMethod.POST, uri, responseType);
    }

    public static <T> Route<T> put(String uri, Class<T> responseType) {
        return new Route<>(HttpMethod.PUT, uri, responseType);
    }

    public static <T> Route<T> patch(String uri, Class<T> responseType) {
        return new Route<>(HttpMethod.PATCH, uri, responseType);
    }

    public static <T> Route<T> delete(String uri, Class<T> responseType) {
        return new Route<>(HttpMethod.DELETE, uri, responseType);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Class<T> getResponseType() {
        return responseType;
    }

    /**
     * Prepare a request, expanding this route template URI with the given parameters.
     *
     * @param uriVars the values to expand each template parameter
     * @return a request that is ready to be routed
     * @see discord4j.rest.request.DiscordRequest#exchange
     */
    public DiscordRequest<T> newRequest(Object... uriVars) {
        return new DiscordRequest<>(this, RouteUtils.expand(getUriTemplate(), uriVars));
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, responseType, uriTemplate);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }

        if (!obj.getClass().isAssignableFrom(Route.class)) {
            return false;
        }

        Route other = (Route) obj;

        return other.method.equals(method) && other.responseType.equals(responseType)
                && other.uriTemplate.equals(uriTemplate);
    }

    @Override
    public String toString() {
        return "Route{" +
                "method=" + method +
                ", uriTemplate='" + uriTemplate + '\'' +
                ", responseType=" + responseType +
                '}';
    }
}
