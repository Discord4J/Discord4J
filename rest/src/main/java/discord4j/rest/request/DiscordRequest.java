package discord4j.rest.request;

import discord4j.rest.route.Route;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import javax.annotation.Nullable;

public class DiscordRequest<T> {

	protected final MonoProcessor<T> mono = MonoProcessor.create();
	private final Route<T> route;
	private final String completeUri;
	private final Bucket bucket;

	@Nullable
	private Object body;

	public DiscordRequest(Route<T> route, String completeUri) {
		this.route = route;
		this.completeUri = completeUri;
		this.bucket = Bucket.of(route.getUri(), completeUri);
	}

	public Route<T> getRoute() {
		return route;
	}

	public HttpMethod getMethod() {
		return route.getMethod();
	}

	public String getUri() {
		return completeUri;
	}

	public Class<T> getResponseType() {
		return route.getResponseType();
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

	public Bucket getBucket() {
		return bucket;
	}

	public Mono<T> exchange(Router router) {
		return router.exchange(this);
	}
}
