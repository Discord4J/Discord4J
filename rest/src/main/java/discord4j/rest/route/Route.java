package discord4j.rest.route;

import discord4j.rest.request.DiscordRequest;
import discord4j.rest.util.UrlBuilder;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Objects;

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

	Route(HttpMethod method, String uri, Class<T> responseType) {
		this.method = method;
		this.uri = uri;
		this.responseType = responseType;
	}

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

	public Route<T> query(String key, Object value) {
		char c;
		if (getUri().contains("?")) {
			c = '&';
		} else {
			c = '?';
		}
		String newUri = getUri() + c + key + "=" + value;

		return new Route<>(getMethod(), newUri, getResponseType());
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

	public DiscordRequest<T> newRequest(Object... uriVars) {
		return new DiscordRequest<>(this, UrlBuilder.expand(getUri(), uriVars), getUri().indexOf("{"));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;

		if (!obj.getClass().isAssignableFrom(Route.class)) return false;

		Route other = (Route) obj;

		return other.method.equals(method) && other.responseType.equals(responseType) && other.uri.equals(uri);
	}

	@Override
	public int hashCode() {
		return Objects.hash(method, responseType, uri);
	}
}
