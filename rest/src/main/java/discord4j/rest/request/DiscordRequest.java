package discord4j.rest.request;

import discord4j.rest.route.Route;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

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

	public DiscordRequest<T> body(Object body) {
		this.body = body;
		return this;
	}

	public DiscordRequest<T> query(String key, Object value) {
		if (queryParams == null) {
			queryParams = new LinkedHashMap<>();
		}
		queryParams.put(key, value);
		return this;
	}

	public Mono<T> exchange(Router router) {
		return router.exchange(this);
	}
}
