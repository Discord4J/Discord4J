package discord4j.rest.request;

import discord4j.rest.http.client.SimpleHttpClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Facilitates the routing of {@link discord4j.rest.request.DiscordRequest DiscordRequests} to the proper
 * {@link discord4j.rest.request.RequestStream RequestStream} according to the bucket in which the request falls.
 */
public class Router {

	private final SimpleHttpClient httpClient;
	private final GlobalRateLimiter globalRateLimiter = new GlobalRateLimiter();
	private final Map<BucketKey, RequestStream<?>> streamMap = new ConcurrentHashMap<>();

	public Router(SimpleHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public <T> Mono<T> exchange(DiscordRequest<T> request) {
		if (request.isExchanged()) {
			throw new IllegalStateException("Attempt to exchange request twice.");
		}

		return Mono.defer(() -> {
			RequestStream<T> stream = getStream(request);
			stream.push(request);
			request.setExchanged(true);
			return request.mono();
		}).cache();
	}

	@SuppressWarnings("unchecked")
	private <T> RequestStream<T> getStream(DiscordRequest<T> request) {
		return (RequestStream<T>)
				streamMap.computeIfAbsent(BucketKey.of(request.getRoute().getUriTemplate(), request.getCompleteUri()),
						k -> {
					RequestStream<T> stream = new RequestStream<>(httpClient, globalRateLimiter);
					stream.start();
					return stream;
				});
	}
}
