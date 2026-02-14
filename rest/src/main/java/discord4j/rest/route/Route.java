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

import discord4j.rest.request.DiscordWebRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Provides a mapping between a Discord API endpoint and its response type.
 */
public class Route {

    private final HttpMethod method;
    private final String uriTemplate;

    private Route(HttpMethod method, String uriTemplate) {
        this.method = method;
        this.uriTemplate = uriTemplate;
    }

    public static Route get(String uri) {
        return new Route(HttpMethod.GET, uri);
    }

    public static Route post(String uri) {
        return new Route(HttpMethod.POST, uri);
    }

    public static Route put(String uri) {
        return new Route(HttpMethod.PUT, uri);
    }

    public static Route patch(String uri) {
        return new Route(HttpMethod.PATCH, uri);
    }

    public static Route delete(String uri) {
        return new Route(HttpMethod.DELETE, uri);
    }

    /**
     * Return the HTTP method for this route.
     *
     * @return the {@link HttpMethod} of this {@link Route}
     */
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * Prepare a request, expanding this route template URI with the given parameters.
     *
     * @param uriVars the values to expand each template parameter
     * @return a request that is ready to be routed
     * @see DiscordWebRequest#exchange
     */
    public DiscordWebRequest newRequest(Object... uriVars) {
        return new DiscordWebRequest(this, uriVars);
    }

    /**
     * Return the URI template that defines this route.
     *
     * @return a URI template, probably containing path parameters, that is defining this {@link Route}
     */
    public String getUriTemplate() {
        return uriTemplate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, uriTemplate);
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

        return other.method.equals(method) && other.uriTemplate.equals(uriTemplate);
    }

    @Override
    public String toString() {
        return "Route{" +
                "method=" + method +
                ", uriTemplate='" + uriTemplate + '\'' +
                '}';
    }
}
