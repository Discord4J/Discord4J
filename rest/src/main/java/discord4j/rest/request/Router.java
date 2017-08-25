package discord4j.rest.request;

import discord4j.rest.http.client.SimpleHttpClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Router {

	private final SimpleHttpClient httpClient;
	private final Map<Bucket, RequestStream<?>> streamMap = new ConcurrentHashMap<>();

	public Router(SimpleHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public <T> Mono<T> exchange(DiscordRequest<T> request) {
		return from(request).push(request);
	}

	@SuppressWarnings("unchecked")
	private <T> RequestStream<T> from(DiscordRequest<T> request) {
		return (RequestStream<T>) streamMap.computeIfAbsent(request.getBucket(), k -> {
			RequestStream<T> stream = new RequestStream<>();
			subscribeTo(stream);
			return stream;
		});
	}

	private <T> void subscribeTo(RequestStream<T> stream) {
		stream.getStream().subscribe(req -> {
			stream.pause();
			httpClient.exchange(req.getMethod(), req.getUri(), req.getBody(), req.getResponseType()).materialize()
					.subscribe(signal -> {
						if (signal.isOnSubscribe()) {
							req.mono.onSubscribe(signal.getSubscription());
						} else if (signal.isOnNext()) {
							req.mono.onNext(signal.get());
						} else if (signal.isOnError()) {
							req.mono.onError(signal.getThrowable());
						} else if (signal.isOnComplete()) {
							req.mono.onComplete();
						}
						stream.resume();
					});
		});
	}
}
