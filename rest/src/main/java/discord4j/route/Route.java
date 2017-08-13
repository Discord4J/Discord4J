package discord4j.route;

import discord4j.util.UrlBuilder;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Map;

/**
 * Provides a mapping between a Discord API endpoint and its response type.
 *
 * @param <T> the response type
 * @since 3.0
 */
public class Route<T> {

    private final HttpMethod method;
    private final String uri;
    private final Class<T> responseType;

    public static <R> Route<R> get(String uri, Class<R> responseType) {
        return new Route<>(HttpMethod.GET, uri, responseType);
    }

    public static <R> Route<R> post(String uri, Class<R> responseType) {
        return new Route<>(HttpMethod.POST, uri, responseType);
    }

    public static <R> Route<R> put(String uri, Class<R> responseType) {
        return new Route<>(HttpMethod.PUT, uri, responseType);
    }

    public static <R> Route<R> patch(String uri, Class<R> responseType) {
        return new Route<>(HttpMethod.PATCH, uri, responseType);
    }

    public static <R> Route<R> delete(String uri, Class<R> responseType) {
        return new Route<>(HttpMethod.DELETE, uri, responseType);
    }

    private Route(HttpMethod method, String uri, Class<T> responseType) {
        this.method = method;
        this.uri = uri;
        this.responseType = responseType;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public Class<T> getResponseType() {
        return responseType;
    }

    // TODO: to decide between the following - what is the best way to complete/expand a route

    public Route<T> complete(Object... parameters) {
        return new Route<>(this.method, UrlBuilder.expand(this.uri, parameters), this.responseType);
    }

    public Route<T> complete(Map<String, ?> parameters) {
        return new Route<>(this.method, UrlBuilder.expand(this.uri, parameters), this.responseType);
    }

    public Route<T> complete(Map<String, ?> queryParameters, Object... uriVariables) {
        return new Route<>(this.method, UrlBuilder.expand(this.uri, queryParameters, uriVariables), this.responseType);
    }
}
