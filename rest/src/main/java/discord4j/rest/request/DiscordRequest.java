package discord4j.rest.request;

import discord4j.rest.route.Route;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import javax.annotation.Nullable;

/**
 * Encodes all of the needed information to make an HTTP request to Discord.
 * <p>
 * When exchanged on a {@link discord4j.rest.request.Router Router}, the request's {@link #mono mono} receives signals
 * based on the response of the request.
 * <p>
 * More than one call to {@link Router#exchange(DiscordRequest)} for the same request is illegal.
 *
 * @param <T> The response type.
 * @since 3.0
 */
public class DiscordRequest<T> {

	protected final MonoProcessor<T> mono = MonoProcessor.create();
	private final Route<T> route;
	private final String completeUri;

	@Nullable
	private Object body;
	private boolean exchanged = false;

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

	public DiscordRequest<T> body(Object body) {
		this.body = body;
		return this;
	}

	public Mono<T> mono() {
		return mono;
	}

	public Mono<T> exchange(Router router) {
		return router.exchange(this);
	}

	boolean isExchanged() {
		return exchanged;
	}

	void setExchanged(boolean exchanged) {
		if (this.exchanged) {
			throw new IllegalStateException("Attempt to set exchanged value twice.");
		}
		this.exchanged = exchanged;
	}
}
