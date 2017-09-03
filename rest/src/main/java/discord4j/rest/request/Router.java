package discord4j.rest.request;

import discord4j.rest.http.client.SimpleHttpClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Router {

	final SimpleHttpClient httpClient;
	final GlobalRateLimiter globalRateLimiter = new GlobalRateLimiter();
	private final Map<Bucket, RequestStream<?>> streamMap = new ConcurrentHashMap<>();

	public Router(SimpleHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public <T> Mono<T> exchange(DiscordRequest<T> request) {
		return Mono.defer(() -> {
			RequestStream<T> stream = getStream(request);
			stream.push(request);
			return request.mono();
		}).cache();
	}

	@SuppressWarnings("unchecked")
	private <T> RequestStream<T> getStream(DiscordRequest<T> request) {
		return (RequestStream<T>) streamMap.computeIfAbsent(request.getBucket(), k -> {
			RequestStream<T> stream = new RequestStream<>(this);
			stream.start();
			return stream;
		});
	}
}
